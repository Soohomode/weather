package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Diary;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

// JPA 방식 레포지토리
@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> { // 레포지토리는 DB와 맞닿아있다

    // 리턴타입 함수명(JPA)(매개변수 파라미터);

    List<Diary> findAllByDate(LocalDate date); // 이렇게만 쳐도 함수가 완성된다 (조회)

    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate); // 몇일 부터 몇일까지 조회

    Diary getFirstByDate(LocalDate date); // 다이어리 수정

    @Transactional
    void deleteAllByDate(LocalDate date); // 다이어리 삭제

}
