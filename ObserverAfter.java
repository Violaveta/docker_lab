// 4.3 Реализация Observer для варианта 5 (Автотранспортное предприятие: событие «путевой лист закрыт»)
import java.util.ArrayList;
import java.util.List;

public class ObserverAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        DispatchCenter dispatchCenter = new DispatchCenter("АТП №5");

        // Три базовых наблюдателя: бухгалтерия, журнал (накапливает записи), служба учёта ГСМ
        AccountingService accounting = new AccountingService();
        WaybillJournal journal = new WaybillJournal();
        FuelControlService fuelControl = new FuelControlService();

        dispatchCenter.addListener(accounting);
        dispatchCenter.addListener(journal);
        dispatchCenter.addListener(fuelControl);

        System.out.println("--- Событие 1: закрыт путевой лист ПЛ-104 (все 3 подписчика) ---");
        dispatchCenter.closeWaybill("ПЛ-104");

        // Проверка 1: после одного события журнал получил ровно одну запись
        check("после одного события журнал получил ровно одну запись", journal.getEntries().size() == 1);

        // Проверка 3: все подписчики получили одно и то же событие
        check("все подписчики получили одно и то же событие ПЛ-104",
                "ПЛ-104".equals(accounting.getLastWaybill())
                        && "ПЛ-104".equals(fuelControl.getLastWaybill())
                        && journal.getEntries().get(0).contains("ПЛ-104"));

        // Отписываем службу учёта ГСМ
        dispatchCenter.removeListener(fuelControl);

        System.out.println();
        System.out.println("--- Событие 2: закрыт путевой лист ПЛ-105 (служба ГСМ отписана) ---");
        dispatchCenter.closeWaybill("ПЛ-105");

        // Проверка 2: после отписки наблюдателя его записи больше не появляются
        check("после отписки служба ГСМ не получила новое событие",
                "ПЛ-104".equals(fuelControl.getLastWaybill()) && fuelControl.getNotificationCount() == 1);
        check("журнал продолжил получать события (теперь 2 записи)", journal.getEntries().size() == 2);

        // Четвёртый наблюдатель (передача в 1С/АСУ) добавляется без правки класса DispatchCenter
        ErpSyncService erpSync = new ErpSyncService();
        dispatchCenter.addListener(erpSync);

        System.out.println();
        System.out.println("--- Событие 3: закрыт путевой лист ПЛ-106 (добавлен 4-й наблюдатель: АСУ) ---");
        dispatchCenter.closeWaybill("ПЛ-106");

        // Проверка 4: добавление четвёртого наблюдателя не потребовало изменения DispatchCenter
        check("четвёртый наблюдатель (АСУ) получил событие без правки DispatchCenter",
                erpSync.getSyncedWaybills().contains("ПЛ-106"));

        System.out.println();
        System.out.println("Порядок получения события подписчиками: строго в порядке их добавления в список (ArrayList) — Бухгалтерия -> Журнал -> АСУ.");

        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }
}

// --- участники паттерна Observer ---

/** Интерфейс наблюдателя за закрытием путевых листов. */
interface WaybillListener {
    void onWaybillClosed(String enterpriseName, String waybillNumber);
}

/** Субъект: диспетчерская АТП. Не знает конкретных реализаций наблюдателей. */
class DispatchCenter {
    private final String enterpriseName;
    private final List<WaybillListener> listeners = new ArrayList<>();

    DispatchCenter(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    void addListener(WaybillListener listener) {
        listeners.add(listener);
    }

    void removeListener(WaybillListener listener) {
        listeners.remove(listener);
    }

    void closeWaybill(String waybillNumber) {
        for (WaybillListener listener : listeners) {
            listener.onWaybillClosed(enterpriseName, waybillNumber);
        }
    }
}

// --- три базовых наблюдателя ---

class AccountingService implements WaybillListener {
    private String lastWaybill;

    public void onWaybillClosed(String enterpriseName, String waybillNumber) {
        this.lastWaybill = waybillNumber;
        System.out.println("[бухгалтерия] " + enterpriseName + ": начисление зарплаты по листу " + waybillNumber);
    }

    String getLastWaybill() {
        return lastWaybill;
    }
}

class WaybillJournal implements WaybillListener {
    private final List<String> entries = new ArrayList<>();

    public void onWaybillClosed(String enterpriseName, String waybillNumber) {
        String entry = enterpriseName + ": закрыт путевой лист " + waybillNumber;
        entries.add(entry);
        System.out.println("[журнал]      " + entry);
    }

    List<String> getEntries() {
        return entries;
    }
}

class FuelControlService implements WaybillListener {
    private String lastWaybill;
    private int notificationCount = 0;

    public void onWaybillClosed(String enterpriseName, String waybillNumber) {
        this.lastWaybill = waybillNumber;
        this.notificationCount++;
        System.out.println("[учёт ГСМ]    " + enterpriseName + ": списание топлива по листу " + waybillNumber);
    }

    String getLastWaybill() {
        return lastWaybill;
    }

    int getNotificationCount() {
        return notificationCount;
    }
}

// --- четвёртый наблюдатель (добавлен без правки DispatchCenter) ---

class ErpSyncService implements WaybillListener {
    private final List<String> syncedWaybills = new ArrayList<>();

    public void onWaybillClosed(String enterpriseName, String waybillNumber) {
        syncedWaybills.add(waybillNumber);
        System.out.println("[АСУ/1С]      " + enterpriseName + ": выгружен путевой лист " + waybillNumber);
    }

    List<String> getSyncedWaybills() {
        return syncedWaybills;
    }
}
