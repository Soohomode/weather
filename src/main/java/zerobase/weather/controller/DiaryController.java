package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController { // 컨트롤러는 클라이언트와 맞닿아 있다.

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) { // 생성자
        this.diaryService = diaryService;
    }

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장", notes = "이것은 노트") // swagger 에서 api 설명란이다
    // 다이어리 만들기
    @PostMapping("/create/diary") // 이 path(경로?)로 요청 보내면 createDiary 메서드가 동작
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestBody String text) {
        //RequestParam은 요청을 보낼때 파라미터도 보냄 여기선 date를 보냄
        diaryService.createDiary(date, text);
    }

    // 다이어리 조회
    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diary") // api
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회할 날짜", example = "2020-02-02") LocalDate date) { // url에 ?(물음표) 뒤에 붙는 파라미터를 뜻한다
        return diaryService.readDiary(date);
    }

    // 다이어리 조회 (날짜 범위를 정한 조회 ex. 5월 한달동안의 일기, 일주일동안의 일기 등)
    @ApiOperation("선택한 기간중의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회할 기간의 첫번째날", example = "2020-01-02") LocalDate startDate,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                            @ApiParam(value = "조회할 기간의 마지막날", example = "2020-02-02") LocalDate endDate) { // 몇월 며칠부터 몇월 며칠까지
        return diaryService.readDiaries(startDate, endDate);
    }

    // 다이어리 수정
    @ApiOperation("일기를 업데이트(수정) 합니다")
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }

    // 다이어리 삭제
    @ApiOperation("일기를 삭제 합니다")
    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
