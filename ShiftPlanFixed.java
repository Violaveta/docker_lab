// 5.1 Исправленный вариант ShiftPlanLegacy по замечаниям из ReviewNotes.csv
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShiftPlanFixed {

    // Замечание 5 и 7: потокобезопасный форматтер и именованная константа вместо магического числа
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final double MAX_SHIFT_HOURS = 10.0;

    // Замечание 5: журнал — поле экземпляра, а не общий статический список
    private final List<String> log = new ArrayList<>();

    private static int failedChecks = 0;

    public static void main(String[] args) {
        ShiftPlanFixed planner = new ShiftPlanFixed();

        List<Brigade> brigades = new ArrayList<>();
        brigades.add(new Brigade("Иванов", 11.0, true));
        brigades.add(new Brigade("Петров", 0.0, true));
        brigades.add(new Brigade("Сидоров", 4.5, false));
        brigades.add(new Brigade("Козлов", 8.0, true));

        System.out.println("--- Самопроверки исправленного кода ---");

        // Проверка 1 (характеризующий тест): корректный состав наряда и сохранность исходного списка
        String plan = planner.buildShiftPlan(brigades, 2);
        check("корректный состав наряда (Петров; Козлов; )", "Петров; Козлов; ".equals(plan));
        check("исходный список бригад не изменён (осталось 4 элемента)", brigades.size() == 4);

        // Проверка 2: граничное значение 10.0 часов (бригада должна допускаться)
        List<Brigade> boundaryList = List.of(new Brigade("Смирнов", 10.0, true));
        check("граничное значение 10.0 часов допускается в наряд",
                "Смирнов; ".equals(planner.buildShiftPlan(boundaryList, 1)));

        // Проверка 3: нехватка бригад вызывает IllegalArgumentException
        boolean shortageCaught = false;
        try {
            planner.buildShiftPlan(brigades, 3);
        } catch (IllegalArgumentException e) {
            shortageCaught = true;
        }
        check("при нехватке доступных бригад выбрасывается IllegalArgumentException", shortageCaught);

        // Проверка 4: некорректные аргументы (need <= 0 или null) вызывают IllegalArgumentException
        boolean invalidArgsCaught = false;
        try {
            planner.buildShiftPlan(brigades, 0);
        } catch (IllegalArgumentException e) {
            invalidArgsCaught = true;
        }
        boolean nullListCaught = false;
        try {
            planner.buildShiftPlan(null, 1);
        } catch (IllegalArgumentException e) {
            nullListCaught = true;
        }
        check("при некорректных аргументах (0 или null) выбрасывается IllegalArgumentException",
                invalidArgsCaught && nullListCaught);

        // Проверка 5: журнал фиксирует события с датой
        check("журнал экземпляра содержит записи о формировании нарядов", planner.getLog().size() == 2);

        // Проверка 6: запись в файл через try-with-resources работает корректно
        Path tempFile = Path.of("наряд.txt");
        planner.saveShiftPlan(plan, tempFile);
        boolean fileWrittenCorrectly = false;
        try {
            fileWrittenCorrectly = plan.equals(Files.readString(tempFile));
        } catch (IOException e) {
            fileWrittenCorrectly = false;
        }
        check("наряд корректно сохранён в файл", fileWrittenCorrectly);

        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    /**
     * Замечание 6 и 8: чистый метод формирования наряда (отделён от записи в файл).
     */
    public String buildShiftPlan(List<Brigade> brigades, int requiredCount) {
        // Замечание 2: проверка входных аргументов
        if (brigades == null) {
            throw new IllegalArgumentException("Список бригад не может быть null");
        }
        if (requiredCount <= 0) {
            throw new IllegalArgumentException("Требуемое количество бригад должно быть больше 0");
        }

        // Замечание 1 и 7: отбор в новый список без изменения исходного, без магических чисел
        List<Brigade> available = new ArrayList<>();
        for (Brigade brigade : brigades) {
            if (brigade != null && brigade.hoursPerShift <= MAX_SHIFT_HOURS && brigade.admitted) {
                available.add(brigade);
            }
        }

        // Замечание 2: проверка на достаточность бригад
        if (available.size() < requiredCount) {
            throw new IllegalArgumentException(
                    "Недостаточно доступных бригад: требуется " + requiredCount + ", доступно " + available.size());
        }

        // Замечание 8: сборка строки через StringBuilder
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < requiredCount; i++) {
            result.append(available.get(i).lastName).append("; ");
        }

        // Замечание 5: потокобезопасное форматирование даты
        log.add(LocalDate.now().format(DATE_FORMATTER) + " наряд сформирован");
        return result.toString();
    }

    /**
     * Замечание 3, 4 и 6: отдельный метод записи в файл с try-with-resources и пробросом ошибки.
     */
    public void saveShiftPlan(String shiftPlan, Path outputPath) {
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(shiftPlan);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка записи наряда в файл: " + outputPath, e);
        }
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }
}

class Brigade {
    final String lastName;
    final double hoursPerShift;
    final boolean admitted;

    Brigade(String lastName, double hoursPerShift, boolean admitted) {
        this.lastName = lastName;
        this.hoursPerShift = hoursPerShift;
        this.admitted = admitted;
    }
}
