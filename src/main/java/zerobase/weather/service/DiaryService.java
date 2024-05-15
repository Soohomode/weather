package zerobase.weather.service;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true) // 읽기전용
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;
    private final DiaryRepository diaryRepository; // 레포지토리 갖고옴
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // "초 분 시 일 월 년" 새벽 1시 마다 동작
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary"); // 로그 남기기

        // 날씨 데이터 가져오기 (API 에서 가져오기? or DB 에서 가져오기)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값 우리 DB에 저장하기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text); // 글

        diaryRepository.save(nowDiary); // 레포지토리에 저장

        logger.info("end to create diary"); // 로그 남기기
    }

    private DateWeather getWeatherFromApi() { // 새벽 1시마다 날씨 받아오기
        // open weather map 에서 데이터 받아오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));

        return dateWeather;
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.size() == 0) {
            // db 에 날씨 정보가 없다면 새로 api에서 날씨 정보를 가져와야한다
            // 과거날씨는 유료이다. 정책상,,, 현재 날씨를 가져오도록 하거나, 날씨없이 일기를 쓰도록,,
            // 현재 날씨를 가져오는걸로 결정
            return getWeatherFromApi();
        } else {
            // db 에 저장된 날씨정보가 있다면
            return dateWeatherListFromDB.get(0);
        }
    }
    // 다이어리 조회하기
    @Transactional(readOnly = true) // 읽기전용
    public List<Diary> readDiary(LocalDate date) {
//        if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
//            throw new InvalidDate();
//        }
        logger.debug("read diary");
        return diaryRepository.findAllByDate(date); // db에서 가져와야함
    }

    // 다이어리 조회 (날짜 범위를 정한 조회 ex. 5월 한달동안의 일기, 일주일동안의 일기 등)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    // 다이어리 수정하기
    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
    }

    // 다이어리 삭제하기
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    // open weather map 에서 데이터 받아오기
    private String getWeatherString() { // open weather map 에서 받아온 데이터를 string으로 반환
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }

    // 받아온 날씨 json 파싱하기 (파싱은 데이터를 이해 가능한 형태로 변환하는 과정)
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
            // 예외를 처리하는게 아닌 예외가 났다고 알려주기만
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main"); // main을 담아서
        resultMap.put("temp", mainData.get("temp")); // main 안의 temp를 map에 넣는다

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather"); // jsonarray 방식으로 넣어야 하기에 변환해서

        JSONObject weatherData = (JSONObject) weatherArray.get(0); // weather를 담아서
        resultMap.put("main", weatherData.get("main")); // weather 안의 main과 icon을 map에
        resultMap.put("icon", weatherData.get("icon")); // 담는다

        return resultMap;

    }

}
