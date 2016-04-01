package uni.miskolc.ips.ilona.measurement.persist.impl.integration;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uni.miskolc.ips.ilona.measurement.model.measurement.BluetoothTags;
import uni.miskolc.ips.ilona.measurement.model.measurement.GPSCoordinate;
import uni.miskolc.ips.ilona.measurement.model.measurement.Magnetometer;
import uni.miskolc.ips.ilona.measurement.model.measurement.Measurement;
import uni.miskolc.ips.ilona.measurement.model.measurement.MeasurementBuilder;
import uni.miskolc.ips.ilona.measurement.model.measurement.WiFiRSSI;
import uni.miskolc.ips.ilona.measurement.model.position.Coordinate;
import uni.miskolc.ips.ilona.measurement.model.position.Position;
import uni.miskolc.ips.ilona.measurement.model.position.Zone;
import uni.miskolc.ips.ilona.measurement.persist.PositionDAO;
import uni.miskolc.ips.ilona.measurement.persist.ZoneDAO;
import uni.miskolc.ips.ilona.measurement.persist.exceptions.InsertionException;
import uni.miskolc.ips.ilona.measurement.persist.exceptions.RecordNotFoundException;
import uni.miskolc.ips.ilona.measurement.persist.mappers.MeasurementMapper;
import uni.miskolc.ips.ilona.measurement.persist.mysql.MySQLMeasurementDAO;
import uni.miskolc.ips.ilona.measurement.persist.mysql.MySQLPositionDAO;
import uni.miskolc.ips.ilona.measurement.persist.mysql.MySQLZoneDAO;

public class MeasurementDAOIntegrationTest extends SetupIntegrationTest{

	private static final Logger LOG = LogManager
			.getLogger("uni.miskolc.ips.ilona.persist");

	private MySQLMeasurementDAO dao;
			
	@Before
	public void setUp(){
		super.setUp();	
		try {
			ZoneDAO zoneDAO = new MySQLZoneDAO(MYBATIS_CONFIG_FILE, HOST, PORT, DATABASE, USER, PASSWORD);
			PositionDAO positionDAO = new MySQLPositionDAO(MYBATIS_CONFIG_FILE, HOST, PORT, DATABASE, USER, PASSWORD, zoneDAO);
			//new MySQLPositionDAO(MYBATIS_CONFIG_FILE, HOST, PORT, DATABASE, USER, PASSWORD, zoneDAO);
			this.dao = new MySQLMeasurementDAO(MYBATIS_CONFIG_FILE, HOST, PORT, DATABASE, USER, PASSWORD, positionDAO);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Assume.assumeNoException(e);
		}
	}
	
	
	@Test
	public void test() throws Exception{
		
		InputStream inputStream = new FileInputStream(MYBATIS_CONFIG_FILE);
		String urlPattern = "jdbc:mysql://%s:%s/%s";
		String connectionURL = String.format(urlPattern, HOST, PORT, DATABASE);
		Properties props = new Properties();
		props.put("driver", "com.mysql.jdbc.Driver");
		props.put("url", connectionURL);
		props.put("username", USER);
		props.put("password", PASSWORD);
		
		SqlSessionFactory sessionFactory = (new SqlSessionFactoryBuilder()).build(
				inputStream, props);
		SqlSession session = sessionFactory.openSession();
		try{
			
			MeasurementMapper mapper = session.getMapper(MeasurementMapper.class);
			System.out.println(mapper.selectPositionById("eb264eea-4106-46a3-9992-70f16f283a15"));
			Set<String> bttags = mapper.selectBTTagsForMeasurement("43c6270b-2d12-49f3-ac8f-0d09d55fcf61"); 
			System.out.println(bttags+"\t"+bttags.getClass().getName());
			
			Map<String, Double> rssi = mapper.selectWiFiRSSIForMeasurement("43c6270b-2d12-49f3-ac8f-0d09d55fcf61");
			System.out.println(rssi);
			System.out.println(rssi.getClass().getName());
			System.out.println("RFID TAGS");
			Set<byte[]> rfidTags = mapper.selectRFIDTagsForMeasurement("43c6270b-2d12-49f3-ac8f-0d09d55fcf61");
			System.out.println("------------->"+rfidTags);
			System.out.println("Class: ===>>>"+rfidTags.toArray().getClass());
			System.out.println(rfidTags.toArray());
			for(byte[] tag : rfidTags){
				String tagStr = "";
				for(int i = 0; i < tag.length; i++){
					tagStr += tag[i];
				}
				System.out.println(tagStr);
			}
			System.out.println("-------------");
			
			Collection<Measurement> measuerements = mapper.selectMeasurements();
			for(Measurement meas : measuerements){
				System.out.println(meas);
			}
			
			Measurement newMeas = new Measurement();
			newMeas.setId(UUID.randomUUID());
			newMeas.setTimestamp(new java.util.Date());
			newMeas.setMagnetometer(new Magnetometer(4, 4, 4, 4));
			newMeas.setGpsCoordinates(new GPSCoordinate(3, 2, 1));
			mapper.insertMeasurement(newMeas);
			Map<String,Double> wifirssi = new HashMap<String,Double>();
			wifirssi.put("testAP1", -1.0);
			wifirssi.put("testAP2", -2.0);
			wifirssi.put("testAP3", -3.0);
			newMeas.setWifiRSSI(new WiFiRSSI(wifirssi));
			newMeas.setBluetoothTags(new BluetoothTags(new HashSet<>(Arrays.asList("bt1", "bt2", "bt3"))));
			
			session.commit();
			System.out.println(newMeas);
			
		}
		finally{
			session.close();
		}
	}
	
	
	
	@Test()
	public void testReadMeasurementFromArray () throws RecordNotFoundException, InsertionException{
	UUID zoneId1 = UUID.fromString("9ff78a6a-2216-4f38-bfeb-5fa189b6421b");
	Zone bathroom = new Zone("bathroom");
	bathroom.setId(zoneId1);
	UUID zoneId2 = UUID.fromString("743d2365-2eaa-412f-8324-6b6b1361ba5b");
	Zone kitchen = new Zone("kitchen");
	kitchen.setId(zoneId2);
	UUID zoneId3 = UUID.fromString("183f0204-5029-4b33-a128-404ba5c68fa8");
	Zone bedroom = new Zone("bedroom");
	bedroom.setId(zoneId3);
	
	UUID posId1 = UUID.fromString("eb264eea-4106-46a3-9992-70f16f283a15");
	Position pos1= new Position(new Coordinate(0,0,0), bathroom);
	pos1.setUUID(posId1);
	UUID posId2 = UUID.fromString("5f484241-6dcc-4731-846c-7fc3e4f0fafb");
	Position pos2= new Position(new Coordinate(0,0,0), kitchen);
	pos2.setUUID(posId2);
	UUID posId3 = UUID.fromString("c36e7f61-ba7b-408f-b113-c528980e7131");
	Position pos3= new Position(new Coordinate(0,0,0), bathroom);
	pos3.setUUID(posId3);
	
	UUID mesID1 = UUID.fromString("59d46ae9-e0c8-48d0-b14a-503ed414b7cc");
	MeasurementBuilder mesbuild1 = new MeasurementBuilder();
	mesbuild1.setPosition(pos1);
	mesbuild1.setMagnetometer(new Magnetometer(0,0,0,0));
	mesbuild1.setGPSCoordinates(new GPSCoordinate(00, 00, 00));
	Measurement mes1 = mesbuild1.build();
	mes1.setTimestamp(Timestamp.valueOf("2015-07-10 15:00:00"));
	mes1.setId(mesID1);
	
	UUID mesID2 = UUID.fromString("59d46ae9-e0c8-48d0-b14a-503ed414b7cc");
	MeasurementBuilder mesbuild2 = new MeasurementBuilder();
	mesbuild2.setPosition(pos2);
	mesbuild2.setMagnetometer(new Magnetometer(1,1,1,1));
	mesbuild2.setGPSCoordinates(new GPSCoordinate(11, 11, 11));
	Measurement mes2 = mesbuild2.build();
	mes2.setTimestamp(Timestamp.valueOf("2015-07-11 20:00:00"));
	mes2.setId(mesID2);
	
	UUID mesID3 = UUID.fromString("59d46ae9-e0c8-48d0-b14a-503ed414b7cc");
	MeasurementBuilder mesbuild3 = new MeasurementBuilder();
	mesbuild3.setPosition(pos3);
	mesbuild3.setMagnetometer(new Magnetometer(2,2,2,2));
	mesbuild3.setGPSCoordinates(new GPSCoordinate(22, 22, 22));
	Measurement mes3 = mesbuild3.build();
	mes3.setTimestamp(Timestamp.valueOf("2015-07-12 12:00:00"));
	mes3.setId(mesID3);
	
	Collection<Measurement> expected=Arrays.asList(mes1, mes2, mes3);
	Collection<Measurement> actual=dao.readMeasurements();
	assertThat(actual,
			hasItems(expected.toArray(new Measurement[expected.size()])));
	}
	
	@Test
	public void testCreateMeasurement() throws InsertionException{
		Measurement measurement = new Measurement();
		UUID measId = UUID.randomUUID();
		measurement.setId(measId);
		measurement.setTimestamp(new java.util.Date());
		measurement.setMagnetometer(new Magnetometer(4, 4, 4, 4));
		measurement.setGpsCoordinates(new GPSCoordinate(3, 2, 1));
		Map<String,Double> wifirssi = new HashMap<String,Double>();
		wifirssi.put("testAP1", -1.0);
		wifirssi.put("testAP2", -2.0);
		wifirssi.put("testAP3", -3.0);
		measurement.setWifiRSSI(new WiFiRSSI(wifirssi));
		measurement.setBluetoothTags(new BluetoothTags(new HashSet<>(Arrays.asList("bt1", "bt2", "bt3"))));
		measurement.setPosition(new Position(new Coordinate(4, 5, 6), new Zone("World End's Tavern")));
		
		dao.createMeasurement(measurement);
		System.out.println(measurement);
		
	}

	@Ignore
	@Test
	public void testDeleteMeasurementByMeasurement() throws RecordNotFoundException {
		UUID zoneId1 = UUID.fromString("9ff78a6a-2216-4f38-bfeb-5fa189b6421b");
		Zone bathroom = new Zone("bathroom");
		bathroom.setId(zoneId1);
		
		UUID posId1 = UUID.fromString("eb264eea-4106-46a3-9992-70f16f283a15");
		Position pos1= new Position(new Coordinate(0,0,0), bathroom);
		pos1.setUUID(posId1);
		
		UUID mesID1 = UUID.fromString("59d46ae9-e0c8-48d0-b14a-503ed414b7cc");
		MeasurementBuilder mesbuild1 = new MeasurementBuilder();
		mesbuild1.setPosition(pos1);
		mesbuild1.setMagnetometer(new Magnetometer(0,0,0,0));
		mesbuild1.setGPSCoordinates(new GPSCoordinate(00, 00, 00));
		Measurement mes1 = mesbuild1.build();
		mes1.setTimestamp(Timestamp.valueOf("2015-07-10 15:00:00"));
		mes1.setId(mesID1);
		
		dao.deleteMeasurement(mes1);
		
		assertEquals(2, dao.readMeasurements().size());
	}
	
	@Ignore
	@Test
	public void testDeleteMeasurementByTimeStamp() throws RecordNotFoundException {
		Timestamp stamp = Timestamp.valueOf("2015-07-10 15:00:00");
		
		Date date = new Date(stamp.getTime());
		
		dao.deleteMeasurement(date);
		
		assertEquals(2, dao.readMeasurements().size());
	}
}