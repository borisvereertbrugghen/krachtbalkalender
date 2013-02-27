package be.avezoete.boris.krachtbal.kalender.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import be.avezoete.boris.krachtbal.kalender.shared.Club;
import be.avezoete.boris.krachtbal.kalender.shared.Datum;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderItem;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderKBException;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderSelectionOptions;
import be.avezoete.boris.krachtbal.kalender.shared.Reeks;

public class KalenderProviderImpl {
	private Pattern reeks_pat = Pattern.compile(".*\\((.+)\\)\\s*");
	private Pattern ploeg_pat = Pattern.compile("\\s*(.*?)\\s*\\((.+)\\)\\s*");
	private Pattern datum_pat = Pattern.compile("(\\d{1,2})\\s*(?:-\\s*(\\d{1,2}))?\\s*(\\w{3})");
	private static Map<String, Integer> monthMap = new HashMap<String, Integer>();
	static{
		monthMap.put("sep", 8);
		monthMap.put("okt", 9);
		monthMap.put("nov", 10);
		monthMap.put("dec", 11);
		monthMap.put("jan", 0);
		monthMap.put("feb", 1);
		monthMap.put("maa", 2);
		monthMap.put("apr", 3);
		monthMap.put("mei", 4);
		monthMap.put("jun", 5);
	}
	
	

	private KalenderProviderCache cache;

	public KalenderProviderImpl(KalenderProviderCache cache) {
		this.cache = cache;
	}

	public List<KalenderItem> getKalender(String reeksId)
			throws KalenderKBException {
		List<KalenderItem> itemsForReeks = cache.getKalenderItems(reeksId);
		if(itemsForReeks!=null){
			return itemsForReeks;
		}
		KalenderSelectionOptions selectionOptions = getSelectionOptions();
		itemsForReeks = getItemsForReeks(selectionOptions.getReeksen().get(reeksId),selectionOptions);
		cache.setKalender(reeksId, itemsForReeks);
		return itemsForReeks;
	}
	
	protected List<KalenderItem> getItemsForReeks(Reeks reeks, KalenderSelectionOptions options) throws KalenderKBException{
		Source source = createReeksSource(reeks);
		List<Element> divs = source.getAllElements("div");
		// div with class sub_hoofding_tekst is a change of reek.
		// div with class sub_tekst are divs containing kalende items
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("CET"));
		boolean atReeks = false;
		for (Element div : divs) {
			String divclass = div.getAttributeValue("class");
			if("sub_hoofding_tekst".equals(divclass)){
				String content = div.getContent().toString();
				Matcher matcher = reeks_pat.matcher(content);
				if(matcher.matches()){
					String group = matcher.group(1);
					if(reeks.getName().equals(group)){
						atReeks=true;
					}
				}
			}else if(atReeks ){
				if("sub_tekst".equals(divclass)){
					ArrayList<KalenderItem> resultItems = new ArrayList<>();
					List<Element> rows = div.getAllElements("tr");
					for (Element row : rows) {
						KalenderItem kalenderItem = createItemForRow(reeks, dateFormat, row,options);
						if(kalenderItem!=null){
							resultItems.add(kalenderItem);
						}
					}
					return resultItems;
				}
			}
		}
		throw new KalenderKBException("No items part found");
	}

	protected KalenderItem createItemForRow(Reeks reeks,
			java.text.SimpleDateFormat dateFormat, Element row,KalenderSelectionOptions options) throws KalenderKBException {
		//cols are: datum, nummer, uur, home, -, out, score home, score uit
		//if nt played yet last 2 are concatenated
		List<Element> cols = row.getAllElements("td");
		if(cols.size()==8 || cols.size() == 7){
			String datum = cols.get(0).getContent().toString();
			String nummer = cols.get(1).getContent().toString();
			String uur = cols.get(2).getContent().toString();
			String home = cols.get(3).getContent().toString();
			
			String out = cols.get(5).getContent().toString();
			if("Vrij".equals(home) || "Vrij".equals(out)){
				return null;
			}
			Date date;
			try {
				date = dateFormat.parse(datum+" "+uur);
			} catch (ParseException e) {
				return null;
			}
			Datum foundDatum = null;
			Collection<Datum> values = options.getDatums().values();
			for (Datum testDatum : values) {
				if(testDatum.getStart().getTime()<date.getTime()){
					if(testDatum.getEnd().getTime()>date.getTime()){
						//found!
						foundDatum = testDatum;
					}//not far enough yet
				}else{//to far already (its assumed datums are in order and not overlapping
					break;
				}
			}
			if(foundDatum==null){
				throw new KalenderKBException("No datum found for "+date +" ("+datum+" "+uur+")");
			}
			KalenderItem kalenderItem;
			if(cols.size()==8){
				String scoreH = cols.get(6).getContent().toString();
				int scoreHi = Integer.parseInt(scoreH);
				String scoreO = cols.get(7).getContent().toString();
				int scoreOi = Integer.parseInt(scoreO);
				kalenderItem = new KalenderItem(nummer, reeks,date,foundDatum,home,out,scoreHi,scoreOi);
			}else{
				String comment = cols.get(6).getContent().toString();
				kalenderItem = new KalenderItem(nummer, reeks,date,foundDatum,home,out,comment);
			}
			return kalenderItem;
		}else{
			return null;
		}
	}

	protected Source createReeksSource(Reeks reeks) throws KalenderKBException {
		InputStream inputStream = postPageGoogleThing(reeks);
		Source source;
		try {
			source = new Source(inputStream);
		} catch (IOException e) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site", e);
		}
		return source;
	}
	
	protected InputStream postPageGoogleThing(Reeks reeks) throws KalenderKBException{
		URL url;
		try {
			url = new URL("http://www.krachtbal.be/competitie/kalender.asp");
		} catch (MalformedURLException e) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site",e);
		}

        HttpURLConnection urlConnection;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("POST");
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		} catch (IOException e) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site",e);
		}

        StringBuilder content = new StringBuilder();
        content.append("club=&datum=&reeks=");
        try {
			content.append(URLEncoder.encode(reeks.getKey(), "UTF-8"));
	        OutputStream outputStream = urlConnection.getOutputStream();
	        outputStream.write(content.toString().getBytes("UTF-8"));
	        outputStream.close();
		} catch (IOException e) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site",e);
		}
        // Response....
        int responseCode;
		try {
			responseCode = urlConnection.getResponseCode();
			 InputStream inputStream;
		        if (responseCode == HttpURLConnection.HTTP_OK) {
		          inputStream = urlConnection.getInputStream();
		        } else {
					throw new KalenderKBException(
							"Couldn't fetch data from KB kalender site responsecode:"+responseCode);
		        }
		        return inputStream;
		} catch (IOException e) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site",e);
		}
       
	}

	protected InputStream postPageHttpClient(Reeks reeks) throws KalenderKBException {
		HttpClient client = new DefaultHttpClient(new SingleClientConnManager());
		HttpPost httppost = new HttpPost("http://www.krachtbal.be/competitie/kalender.asp");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("club", ""));
        nameValuePairs.add(new BasicNameValuePair("reeks", reeks.getKey()));
        nameValuePairs.add(new BasicNameValuePair("datum", ""));
        try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e2) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site", e2);
		}

		HttpResponse execute;
		InputStream inputStream;
		try {
			execute = client.execute(httppost);
			inputStream = execute.getEntity().getContent();
		} catch (IOException e1) {
			throw new KalenderKBException(
					"Couldn't fetch data from KB kalender site", e1);
		}
		return inputStream;
	}

	public KalenderSelectionOptions getSelectionOptions()
			throws KalenderKBException {
		KalenderSelectionOptions options = cache.getSelectionOptions();
		if(options==null){
			Source source;
			try {
				source = new Source(new URL(
						"http://www.krachtbal.be/competitie/kalender.asp"));
			} catch (IOException e2) {
				throw new KalenderKBException(
						"Couldn't fetch data from KB kalender site", e2);
			}
			options =  getOptionsFromSource(source);
			cache.setSelectionOptions(options);
		}
		return options;
	}

	protected KalenderSelectionOptions getOptionsFromSource(Source source)
			throws KalenderKBException {
		ArrayList<Club> clubs = null;
		ArrayList<Reeks> reeksen = null;
		ArrayList<Datum> datums = null;
		List<Element> selects = source.getAllElements("select");
		for (Element element : selects) {
			String name = element.getAttributeValue("name");
			if (clubs == null && "club".equals(name)) {
				List<Element> options = element.getAllElements("option");
				clubs = new ArrayList<Club>(options.size());
				for (Element option : options) {
					String optionValue = option.getAttributeValue("value");
					if (optionValue != null && !optionValue.isEmpty()) {
						Segment content = option.getContent();
						String clubnaam = content.toString();
						Matcher matcher = ploeg_pat.matcher(clubnaam);
						if(!matcher.matches()){
							throw new KalenderKBException("Can't find ploegnaam date from '"+clubnaam+"'");
						}
						clubs.add(new Club(matcher.group(1),matcher.group(2), optionValue));
					}
				}
			} else if (reeksen == null && "reeks".equals(name)) {
				List<Element> options = element.getAllElements("option");
				reeksen = new ArrayList<Reeks>(options.size());
				for (Element option : options) {
					String optionValue = option.getAttributeValue("value");
					if (optionValue != null && !optionValue.isEmpty()) {
						Segment content = option.getContent();
						reeksen.add(new Reeks(content.toString(), optionValue));
					}
				}
			} else if (datums == null && "datum".equals(name)) {
				List<Element> options = element.getAllElements("option");
				datums = new ArrayList<Datum>(options.size());
				for (Element option : options) {
					String optionValue = option.getAttributeValue("value");
					if (optionValue != null && !optionValue.isEmpty()) {
						Segment content = option.getContent();
						String dateString = content.toString().trim();
						Matcher matcher = datum_pat.matcher(dateString);
						if(!matcher.matches()){
							throw new KalenderKBException("Can't find start / end date from '"+dateString+"'");
						}
						String startDay=matcher.group(1);
						String endDay;
						String month;
						endDay=matcher.group(2);
						month=matcher.group(3);
						if(endDay==null){
							endDay=startDay;
						}
						Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("CET"), new Locale("be_nl"));
						Integer monthI = monthMap.get(month);
						calendar.set(Calendar.MONTH, monthI);
						calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDay));
						Date now = new Date();
						if(monthI>6){
							if(now.getMonth()>6){//current Year
								calendar.set(Calendar.YEAR,now.getYear()+1900);
							}else{
								calendar.set(Calendar.YEAR,now.getYear()-1+1900);
							}
						}else{
							if(now.getMonth()>6){//next Year
								calendar.set(Calendar.YEAR,now.getYear()+1+1900);
							}else{
								calendar.set(Calendar.YEAR,now.getYear()+1900);
							}
						}
						calendar.set(Calendar.HOUR_OF_DAY, 1);
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
						Date startDate = calendar.getTime();
						calendar.set(Calendar.HOUR_OF_DAY, 23);
						calendar.set(Calendar.MINUTE, 59);
						calendar.set(Calendar.SECOND, 59);
						calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(endDay));
						Date endDate = calendar.getTime();
						datums.add(new Datum(dateString, optionValue,startDate,endDate));
					}
				}
			}
			if (reeksen != null && datums != null && clubs != null) {
				break;
			}
		}
		if (clubs == null || clubs.isEmpty()) {
			throw new KalenderKBException("geen clubs gevonden");
		}
		if (reeksen == null || reeksen.isEmpty()) {
			throw new KalenderKBException("geen reeks gevonden");
		}
		if (datums == null) {
			throw new KalenderKBException("geen datum gevonden");
		}
		return new KalenderSelectionOptions(clubs, reeksen, datums);
	}
}
