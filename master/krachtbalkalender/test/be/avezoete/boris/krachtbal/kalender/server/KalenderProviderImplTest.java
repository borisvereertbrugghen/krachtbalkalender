package be.avezoete.boris.krachtbal.kalender.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import be.avezoete.boris.krachtbal.kalender.shared.Club;
import be.avezoete.boris.krachtbal.kalender.shared.Datum;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderItem;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderSelectionOptions;
import be.avezoete.boris.krachtbal.kalender.shared.Reeks;

public class KalenderProviderImplTest {

	@Test
	public void testGetOptions() throws Exception {
		KalenderProviderImpl kalenderProviderImpl = new KalenderProviderImpl(new KalenderProviderCache());
		KalenderSelectionOptions selectionOptions = kalenderProviderImpl.getSelectionOptions();
		LinkedHashMap<String, Club> clubs = selectionOptions.getClubs();
		Club kbkTemse = clubs.get("42_0");
		assertNotNull(clubs.toString(),kbkTemse);
		assertEquals("42_0", kbkTemse.getKey());
		assertEquals("KBK Temse",kbkTemse.getName());

		LinkedHashMap<String, Reeks> reeksen = selectionOptions.getReeksen();
		Reeks NH1 = reeksen.get("1");

		assertNotNull(reeksen.toString(),NH1);
		assertEquals("1", NH1.getKey());
		assertEquals("1NHA",NH1.getName());
		
		LinkedHashMap<String,Datum> datums = selectionOptions.getDatums();
		Datum datum = datums.get("2");
		assertNotNull(datums.toString(),datum);

		assertEquals(datum.toString(),new Date(1347058800000L), datum.getStart());
		assertEquals(datum.toString(),new Date(1347227999000L), datum.getEnd());
		
		
	}
	
	@Test
	public void testGetKalender() throws Exception {
		KalenderProviderImpl kalenderProviderImpl = new KalenderProviderImpl(new KalenderProviderCache());
		List<KalenderItem> items = kalenderProviderImpl.getKalender("1");//basically if there is no exception its ok...
		assertEquals(items.toString(),132,items.size());
	}
}
