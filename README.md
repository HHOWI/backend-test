# 서버 CPU 사용률 모니터링 시스템 구현

## 데이터 수집 및 저장
OperatingSystemMXBean를 통하여 CPU 사용량 수집

```
public class CpuUsageCollector {

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static double getCpuUsage() {
        return osBean.getSystemCpuLoad() * 100;
    }
}
```
@Scheduled 어노테이션으로 1분마다 수집 및 저장 처리
```
public class SchedulerConfig {

    @Autowired
    private ServerCpuUsageService service;

    @Scheduled(fixedRate = 60000)
    public void collectCpuUsage() {
        try {
            double cpuUsage = CpuUsageCollector.getCpuUsage();
            service.saveCpuUsage(cpuUsage);
        } catch (Exception e) {
            log.error("CPU 사용률 수집에 실패하였습니다", e);
        }

    }
}
```
```
public void saveCpuUsage(double cpuUsage) {
        ServerCpuUsage serverCpuUsage = new ServerCpuUsage();
        serverCpuUsage.setTimestamp(LocalDateTime.now());
        serverCpuUsage.setCpuUsage(cpuUsage);
        dao.save(serverCpuUsage);
    }
```


## 데이터 조회 API
1. 분 단위 조회: 지정한 시간 구간의 분 단위 CPU 사용률을 조회합니다.
매개변수로 LocalDateTime 값을 2개 받아서 두 값의 between 계산하여 처리
```
    @Operation(summary = "특정 시간 사이의 분단위 CPU 사용량 조회")
    @GetMapping("/usage/minute")
    public List<ServerCpuUsage> getMinuteUsage(
            @Parameter(description = "시작 시간 (ISO 형식: YYYY-MM-DDT:hh:mm:ss)", example = "2024-05-26T04:20:15")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "종료 시간 (ISO 형식: YYYY-MM-DDT:hh:mm:ss)", example = "2024-05-26T04:30:15")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.getMinuteUsage(start, end);
    }

    List<ServerCpuUsage> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
```

2. 시 단위 조회: 지정한 날짜의 시  단위 CPU 최소/최대/평균 사용률을 조회합니다.
매개변수로 LocalDate 값을 하나 받아서 그 값에 해당하는 날짜를 where 절을 통해 처리
```
    // 시 단위 조회
    @Operation(summary = "특정 날짜의 시간단위 CPU 사용량 조회")
    @GetMapping("/usage/hour")
    public List<CpuUsageSummary> getHourlyUsage(
            @Parameter(description = "날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-26")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getHourlyUsage(date);
    }

    @Query("SELECT new com.terra.backendtest.domain.CpuUsageSummary(FUNCTION('DAY', s.timestamp), FUNCTION('HOUR', s.timestamp), MIN(s.cpuUsage), MAX(s.cpuUsage), AVG(s.cpuUsage)) " +
            "FROM ServerCpuUsage s " +
            "WHERE CAST(s.timestamp AS date) = :date " +
            "GROUP BY FUNCTION('DAY', s.timestamp), FUNCTION('HOUR', s.timestamp)")
    List<CpuUsageSummary> findHourlyUsage(@Param("date") LocalDate date);
```
3. 일 단위 조회: 지정한 날짜 구간의 일  단위 CPU 최소/최대/평균 사용률을 조회합니다.
매개변수로 LocalDateTime 값을 2개 받아서 두 값의 between 계산하여 처리
```
 @Operation(summary = "특정 두 날짜 사이의 날짜단위 CPU 사용량 조회")
    @GetMapping("/usage/day")
    public List<CpuUsageSummary> getDailyUsage(
            @Parameter(description = "시작 날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-26")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "종료 날짜 (ISO 형식: YYYY-MM-DD", example = "2024-05-27")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.getDailyUsage(start, end);
    }

 @Query("SELECT new com.terra.backendtest.domain.CpuUsageSummary(FUNCTION('DAY', u.timestamp), null as hour, MIN(u.cpuUsage), MAX(u.cpuUsage), AVG(u.cpuUsage)) " +
            "FROM ServerCpuUsage u " +
            "WHERE u.timestamp BETWEEN :start AND :end " +
            "GROUP BY DAY(u.timestamp) " +
            "ORDER BY DAY(u.timestamp)")
    List<CpuUsageSummary> findDailyUsage(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```
4. Swagger를 사용하여 API 문서화를 설정하세요.
SwaggerConfig 파일 통해 title, desc 작성하여 API 문서화하였음.
각 API마다 어노테이션으로 설명 추가하였음
```
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("서버 CPU 사용률 모니터링 시스템")
                .description("서버 CPU 사용률을 분 단위, 시 단위, 일 단위 조회 API");
    }
}
```

## 데이터 제공 기한 및 예외처리
데이터 제공 기한 처리는 서비스레이어에서 조건을 걸어 처리함
```
// 분 단위 조회: 지정한 시간 구간의 분 단위 CPU 사용률을 조회합니다.
    public List<ServerCpuUsage> getMinuteUsage(LocalDateTime start, LocalDateTime end) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        if (start.isBefore(oneWeekAgo)) {
            throw new IllegalArgumentException("최근 1주 데이터만 조회 가능합니다.");
        }
        return dao.findByTimestampBetween(start, end);
    }

    // 시 단위 조회: 지정한 날짜의 시  단위 CPU 최소/최대/평균 사용률을 조회합니다.
    public List<CpuUsageSummary> getHourlyUsage(LocalDate date) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        if (date.isBefore(threeMonthsAgo)) {
            throw new IllegalArgumentException("최근 3달 데이터만 조회 가능합니다.");
        }
        return dao.findHourlyUsage(date);
    }

    // 일 단위 조회: 지정한 날짜 구간의 일  단위 CPU 최소/최대/평균 사용률을 조회합니다.
    public List<CpuUsageSummary> getDailyUsage(LocalDate start, LocalDate end) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (start.isBefore(oneYearAgo)) {
            throw new IllegalArgumentException("최근 1년 데이터만 조회 가능합니다.");
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusSeconds(1);
        return dao.findDailyUsage(startDateTime, endDateTime);
    }
```

데이터 수집 실패와 잘못된 파라미터 값에 대해서는 글로벌익셉션으로 관리
```
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("IllegalArgumentException occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("잘못된 시간 포맷입니다.");
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("잘못된 시간 포맷입니다.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        ex.printStackTrace();
        return "예상치 못한 오류가 발생했습니다. 다시 시도 해주십시오.";
    }
}
```


