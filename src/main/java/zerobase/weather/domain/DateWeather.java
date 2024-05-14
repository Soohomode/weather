package zerobase.weather.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "date_weather") // table 이름
@NoArgsConstructor
public class DateWeather {
    @Id
    private LocalDate date; // pk 키

    private String weather;

    private String icon;

    private double temperature;
}
