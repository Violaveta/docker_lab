// 3.3 Реализация Factory Method для варианта 5 (Автотранспортное предприятие: типы транспортных средств)
import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        // Каталог создателей: вызывающий код работает только с интерфейсами VehicleCreator и Vehicle
        Map<String, VehicleCreator> creatorCatalog = new LinkedHashMap<>();
        creatorCatalog.put("TRUCK", new TruckCreator());
        creatorCatalog.put("BUS", new BusCreator());
        creatorCatalog.put("VAN", new VanCreator());

        System.out.println("--- Создание транспортных средств через фабричные методы ---");
        Vehicle truck = creatorCatalog.get("TRUCK").createVehicle(15.0);
        Vehicle bus = creatorCatalog.get("BUS").createVehicle(4.5);
        Vehicle van = creatorCatalog.get("VAN").createVehicle(2.5);

        System.out.println(truck.description());
        System.out.println(bus.description());
        System.out.println(van.description());

        // Четвёртый тип добавлен новым классом без правки существующих классов
        VehicleCreator refrigeratorCreator = new RefrigeratorTruckCreator();
        Vehicle refrigeratorTruck = refrigeratorCreator.createVehicle(10.0);
        System.out.println(refrigeratorTruck.description());

        System.out.println();
        System.out.println("--- Проверки ---");
        check("описание грузовика не пустое и содержит грузоподъёмность",
                !truck.description().isEmpty() && truck.description().contains("15.0"));
        check("описание автобуса не пустое и содержит грузоподъёмность",
                !bus.description().isEmpty() && bus.description().contains("4.5"));
        check("описание фургона не пустое и содержит грузоподъёмность",
                !van.description().isEmpty() && van.description().contains("2.5"));

        check("TruckCreator создаёт объект типа Truck", truck instanceof Truck);
        check("BusCreator создаёт объект типа Bus", bus instanceof Bus);
        check("VanCreator создаёт объект типа Van", van instanceof Van);

        check("четвёртый тип (рефрижератор) работает наравне с остальными",
                refrigeratorTruck instanceof RefrigeratorTruck
                        && refrigeratorTruck.capacityTons() == 10.0
                        && refrigeratorTruck.description().contains("10.0"));

        finishChecks();
    }

    // --- служебные методы проверки ---

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static void finishChecks() {
        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }
}

// --- участники паттерна Factory Method ---

/** Общий интерфейс продукта: транспортное средство. */
interface Vehicle {
    String description();
    double capacityTons();
}

/** Общий интерфейс создателя с фабричным методом. */
interface VehicleCreator {
    Vehicle createVehicle(double capacityTons);
}

// --- базовые продукты (3 типа) ---

class Truck implements Vehicle {
    private final double capacityTons;

    Truck(double capacityTons) {
        this.capacityTons = capacityTons;
    }

    public String description() {
        return "Грузовой автомобиль: грузоподъёмность " + capacityTons + " т, перевозка крупногабаритных грузов";
    }

    public double capacityTons() {
        return capacityTons;
    }
}

class Bus implements Vehicle {
    private final double capacityTons;

    Bus(double capacityTons) {
        this.capacityTons = capacityTons;
    }

    public String description() {
        return "Автобус: грузоподъёмность " + capacityTons + " т, пассажирские перевозки";
    }

    public double capacityTons() {
        return capacityTons;
    }
}

class Van implements Vehicle {
    private final double capacityTons;

    Van(double capacityTons) {
        this.capacityTons = capacityTons;
    }

    public String description() {
        return "Фургон: грузоподъёмность " + capacityTons + " т, городская доставка малогабаритных грузов";
    }

    public double capacityTons() {
        return capacityTons;
    }
}

// --- конкретные создатели (3 типа) ---

class TruckCreator implements VehicleCreator {
    public Vehicle createVehicle(double capacityTons) {
        return new Truck(capacityTons);
    }
}

class BusCreator implements VehicleCreator {
    public Vehicle createVehicle(double capacityTons) {
        return new Bus(capacityTons);
    }
}

class VanCreator implements VehicleCreator {
    public Vehicle createVehicle(double capacityTons) {
        return new Van(capacityTons);
    }
}

// --- расширение: четвёртый тип без правки старого кода ---

class RefrigeratorTruck implements Vehicle {
    private final double capacityTons;

    RefrigeratorTruck(double capacityTons) {
        this.capacityTons = capacityTons;
    }

    public String description() {
        return "Авторефрижератор: грузоподъёмность " + capacityTons + " т, перевозка скоропортящихся грузов";
    }

    public double capacityTons() {
        return capacityTons;
    }
}

class RefrigeratorTruckCreator implements VehicleCreator {
    public Vehicle createVehicle(double capacityTons) {
        return new RefrigeratorTruck(capacityTons);
    }
}
