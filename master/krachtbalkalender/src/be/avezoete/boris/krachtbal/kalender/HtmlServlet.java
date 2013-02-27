package be.avezoete.boris.krachtbal.kalender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import be.avezoete.boris.krachtbal.kalender.server.KalenderProviderCache;
import be.avezoete.boris.krachtbal.kalender.server.KalenderProviderImpl;
import be.avezoete.boris.krachtbal.kalender.shared.Club;
import be.avezoete.boris.krachtbal.kalender.shared.Datum;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderItem;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderKBException;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderSelection;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderSelectionOptions;
import be.avezoete.boris.krachtbal.kalender.shared.Reeks;

public class HtmlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest jetreq=(HttpServletRequest)req;
		HttpSession session = jetreq.getSession(true);
		KalenderProviderCache cache = getCache(session);
		if(req.getParameter("reload")!=null){
			cache.clear();
			res.getWriter().append("Cleared all data <br>");
		}
		KalenderProviderImpl impl = new KalenderProviderImpl(cache); 
		KalenderSelectionOptions selectionOptions;
		try {
			selectionOptions = impl.getSelectionOptions();
		} catch (KalenderKBException e) {
			throw new ServletException(e);
		}
		final KalenderSelection selection = new KalenderSelection();
		 String[] reeksen = req.getParameterValues("reeks");
		 if(reeksen!=null){
			for (String string : reeksen) {
				selection.getReeksen().add(string);
			}
		 }
		 String[] clubs = req.getParameterValues("club");
		 if(clubs!=null){
			for (String string : clubs) {
				selection.getClubs().add(string);
			}
		 }
		 String[] datums = req.getParameterValues("datum");
		 if(datums!=null){
			for (String string : datums) {
				selection.getDatums().add(string);
			}
		 }else{
			 Collection<Datum> values = selectionOptions.getDatums().values();
			 Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CET"), new Locale("be-nl"));
			 cal.add(Calendar.DAY_OF_MONTH, -2);
			 Date start = cal.getTime();
			 
			 Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("CET"), new Locale("be-nl"));
			 calendar.add(Calendar.WEEK_OF_YEAR, 3);
			 Date end = calendar.getTime();
			 for (Datum datum : values) {
				if(start.before(datum.getStart())&&end.after(datum.getEnd())){
					selection.getDatums().add(datum.getKey());
				}
			}
			 
		 }
		 
		
		

		res.getWriter().append("<form id='selection' method='get'>");
		res.getWriter().append("<div>");
		writeReeksen(res, selectionOptions,selection);
		res.getWriter().append("</div>");
		res.getWriter().append("<div>");
		writeClubs(res, selectionOptions,selection);
		writeDates(res, selectionOptions,selection);
		res.getWriter().append("</div>");
		res.getWriter().append(" <input name='' value='filter' type='submit'> <input name='reload' value='reload' type='submit'> </form>");
		if(selection.getReeksen().isEmpty()){
			return;
		}
		
		LinkedList<KalenderItem> items = new LinkedList<>();
		Set<String> selectedReeksen = selection.getReeksen();
		for (String reeksId : selectedReeksen) {
			try{
				items.addAll(impl.getKalender(reeksId));
			}catch(KalenderKBException e){
				res.getWriter().append("failed fetching data for "+reeksId).append("<br>");
				e.printStackTrace(res.getWriter());
			}
		}

		List<KalenderItem> filtered = new ArrayList<>(items.size());
		Set<String> selClubs = selection.getClubs();
		Set<String> clubStrings = new HashSet<>(selClubs.size());
		for (String club : selClubs) {
			Club theClub = selectionOptions.getClubs().get(club);
			clubStrings.add(theClub.getName());
		}
		for (KalenderItem kalenderItem : items) {
//			if(!selection.getReeksen().isEmpty() && !selection.getReeksen().contains(kalenderItem.getReeks().getKey())){
//				continue;
//			}
			if(!clubStrings.isEmpty() && !clubStrings.contains(kalenderItem.getHome()) &&
					!clubStrings.contains(kalenderItem.getOut())){
				continue;
			}
			if(!selection.getDatums().isEmpty() && !selection.getDatums().contains(kalenderItem.getDatum().getKey())){
				continue;
			}
			filtered.add(kalenderItem);
		}
		for (KalenderItem kalenderItem : filtered) {
			res.getWriter().append(kalenderItem.toString()).append("<br>");
		}
	}


	protected void writeReeksen(ServletResponse res,
			KalenderSelectionOptions selectionOptions, KalenderSelection selection) throws IOException {
		LinkedHashMap<String,Reeks> reeksen = selectionOptions.getReeksen();
		Collection<Reeks> values = reeksen.values();
		
		for (Reeks reeks : values) {
			res.getWriter().append("<input name='reeks' type='checkbox' value='").append(reeks.getKey()).append("'");
			if(selection.getReeksen().contains(reeks.getKey())){
				res.getWriter().append(" checked='checked' ");
			}
			res.getWriter().append(">");
			res.getWriter().append(reeks.getName());
			res.getWriter().append("</input>");
		}
	}
	private void writeDates(ServletResponse res,
			KalenderSelectionOptions selectionOptions,
			KalenderSelection selection) throws IOException {
		LinkedHashMap<String, Datum> dates = selectionOptions.getDatums();
		Collection<Datum> values = dates.values();
		res.getWriter().append("<select name='datum' multiple='multiple' size='20'");
		for (Datum datum : values) {
			res.getWriter().append("<option value='").append(datum.getKey()).append("' ");
			if(selection.getDatums().contains(datum.getKey())){
				res.getWriter().append(" selected='selected' ");
			}
			res.getWriter().append(">");
			res.getWriter().append(datum.getName());
			res.getWriter().append("</option>");
		}
		res.getWriter().append("</select>");
	}

	
	protected void writeClubs(ServletResponse res,
			KalenderSelectionOptions selectionOptions, KalenderSelection selection) throws IOException {
		LinkedHashMap<String, Club> clubs = selectionOptions.getClubs();
		Collection<Club> values = clubs.values();
		res.getWriter().append("<select name='club' multiple='multiple' size='20'");
		for (Club club : values) {
			res.getWriter().append("<option value='").append(club.getKey()).append("' ");
			if(selection.getClubs().contains(club.getKey())){
				res.getWriter().append(" selected='selected' ");
			}
			res.getWriter().append(">");
			res.getWriter().append(club.getName());
			res.getWriter().append("</option>");
		}
		res.getWriter().append("</select>");
	}

	protected KalenderProviderCache getCache(HttpSession session) {
		KalenderProviderCache cache = (KalenderProviderCache)session.getAttribute("providerCache");
		if(cache==null){
			cache = new KalenderProviderCache();
			session.setAttribute("providerCache", cache);
		}
		return cache;
	}

}
