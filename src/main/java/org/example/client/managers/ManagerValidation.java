package org.example.client.managers;

import org.example.client.enums.Colors;
import org.example.packet.collection.Coordinates;
import org.example.packet.collection.Location;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.example.client.Client.managerInputOutput;

public class ManagerValidation {
    public RouteClient validateFromInput() {
        String name = validateSetName();
        Coordinates coordinates = new Coordinates(validateSetCoordinatesX(), validateSetCoordinatesY());
        Location locationFrom = new Location(validateSetLocationX("From"), validateSetLocationY("From"), validateSetLocationZ("From"));
        Location locationTo = new Location(validateSetLocationX("To"), validateSetLocationY("To"), validateSetLocationZ("To"));
        int distance = validateSetDistance();
        BigDecimal price = validateSetPrice();
        return new RouteClient(name, coordinates, locationFrom, locationTo, distance, price);
    }

    public RouteClient validateFromScript() {
        try {
            String name = managerInputOutput.readLineIO();
            if (name == null || name.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: name не может быть пустым\n", Colors.RED);
                return null;
            }

            String xStr = managerInputOutput.readLineIO();
            if (xStr == null || xStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: X не может быть пустым\n", Colors.RED);
                return null;
            }
            long x;
            try {
                x = Long.parseLong(xStr.trim());
                if (x > 108) {
                    managerInputOutput.writeLineIO("Ошибка: X должно быть <= 108\n", Colors.RED);
                    return null;
                }
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: X должно быть целым числом\n", Colors.RED);
                return null;
            }

            String yStr = managerInputOutput.readLineIO();
            if (yStr == null || yStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: Y не может быть пустым\n", Colors.RED);
                return null;
            }
            long y;
            try {
                y = Long.parseLong(yStr.trim());
                if (y > 20) {
                    managerInputOutput.writeLineIO("Ошибка: Y должно быть <= 20\n", Colors.RED);
                    return null;
                }
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: Y должно быть целым числом\n", Colors.RED);
                return null;
            }
            Coordinates coordinates = new Coordinates(x, y);

            String fromXStr = managerInputOutput.readLineIO();
            if (fromXStr == null || fromXStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: From X не может быть пустым\n", Colors.RED);
                return null;
            }
            float fromX;
            try {
                fromX = Float.parseFloat(fromXStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: From X должно быть числом\n", Colors.RED);
                return null;
            }

            String fromYStr = managerInputOutput.readLineIO();
            if (fromYStr == null || fromYStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: From Y не может быть пустым\n", Colors.RED);
                return null;
            }
            Double fromY;
            try {
                fromY = Double.parseDouble(fromYStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: From Y должно быть числом\n", Colors.RED);
                return null;
            }

            String fromZStr = managerInputOutput.readLineIO();
            if (fromZStr == null || fromZStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: From Z не может быть пустым\n", Colors.RED);
                return null;
            }
            Integer fromZ;
            try {
                fromZ = Integer.parseInt(fromZStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: From Z должно быть целым числом\n", Colors.RED);
                return null;
            }
            Location locationFrom = new Location(fromX, fromY, fromZ);

            String toXStr = managerInputOutput.readLineIO();
            if (toXStr == null || toXStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: To X не может быть пустым\n", Colors.RED);
                return null;
            }
            float toX;
            try {
                toX = Float.parseFloat(toXStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: To X должно быть числом\n", Colors.RED);
                return null;
            }

            String toYStr = managerInputOutput.readLineIO();
            if (toYStr == null || toYStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: To Y не может быть пустым\n", Colors.RED);
                return null;
            }
            Double toY;
            try {
                toY = Double.parseDouble(toYStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: To Y должно быть числом\n", Colors.RED);
                return null;
            }

            String toZStr = managerInputOutput.readLineIO();
            if (toZStr == null || toZStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: To Z не может быть пустым\n", Colors.RED);
                return null;
            }
            Integer toZ;
            try {
                toZ = Integer.parseInt(toZStr.trim());
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: To Z должно быть целым числом\n", Colors.RED);
                return null;
            }
            Location locationTo = new Location(toX, toY, toZ);

            String distanceStr = managerInputOutput.readLineIO();
            if (distanceStr == null || distanceStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: distance не может быть пустым\n", Colors.RED);
                return null;
            }
            int distance;
            try {
                distance = Integer.parseInt(distanceStr.trim());
                if (distance <= 1) {
                    managerInputOutput.writeLineIO("Ошибка: distance должно быть больше 1\n", Colors.RED);
                    return null;
                }
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: distance должно быть целым числом\n", Colors.RED);
                return null;
            }

            String priceStr = managerInputOutput.readLineIO();
            if (priceStr == null || priceStr.trim().isEmpty()) {
                managerInputOutput.writeLineIO("Ошибка: price не может быть пустым\n", Colors.RED);
                return null;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(priceStr.trim().replace(',', '.'));
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    managerInputOutput.writeLineIO("Ошибка: price не может быть отрицательным\n", Colors.RED);
                    return null;
                }
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: price должно быть числом\n", Colors.RED);
                return null;
            }

            return new RouteClient(name, coordinates, locationFrom, locationTo, distance, price);

        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка при чтении данных скрипта: " + e.getMessage() + "\n", Colors.RED);
            return null;
        }
    }

    public String validateSetName() {
        while (true) {
            String name = managerInputOutput.readLineIO("Введите name : ");
            if (!name.isEmpty()) return name;
            managerInputOutput.writeLineIO("NAME не может быть null\n");
        }
    }

    public long validateSetCoordinatesX() {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите X для Coordinates : ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("X не может быть пустым\n");
                continue;
            }
            try {
                long x = Long.parseLong(input);
                if (x <= 108) return x;
                managerInputOutput.writeLineIO("X до 108\n");
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: X должно быть целым не больше 108\n");
            }
        }
    }

    public long validateSetCoordinatesY() {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите Y для Coordinates : ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("Y не может быть пустым\n");
                continue;
            }
            try {
                long y = Long.parseLong(input);
                if (y <= 20) return y;
                managerInputOutput.writeLineIO("Y должно быть не больше 20\n");
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: Y должно быть целым не больше 20\n");
            }
        }
    }

    public float validateSetLocationX(String a) {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите X для Location" + a + ": ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("X не может быть пустым\n");
                continue;
            }
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("X должно быть числом в диапазоне от " + Float.MIN_VALUE + " до " + Float.MAX_VALUE + "\n");
            }
        }
    }

    public Double validateSetLocationY(String a) {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите Y для Location" + a + ": ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("Y не может быть пустым\n");
                continue;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Y должно быть числом в диапазоне от " + Double.MIN_VALUE + " до " + Double.MAX_VALUE + "\n");
            }
        }
    }

    public Integer validateSetLocationZ(String a) {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите Z для Location" + a + ": ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("Z не может быть пустым\n");
                continue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Z должно быть целым числом в диапазоне от " + Integer.MIN_VALUE + " до " + Integer.MAX_VALUE + "\n");
            }
        }
    }

    private int validateSetDistance() {
        while (true) {
            String input = managerInputOutput.readLineIO("Введите distance : ").trim();
            if (input.isEmpty()) {
                managerInputOutput.writeLineIO("DISTANCE не может быть пустым\n");
                continue;
            }
            try {
                int distance = Integer.parseInt(input);
                if (distance > 1) return distance;
                managerInputOutput.writeLineIO("DISTANCE должно быть больше 1\n");
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("DISTANCE должно быть целым и в диапазоне от 2 до " + Integer.MAX_VALUE + "\n");
            }
        }
    }

    private BigDecimal validateSetPrice() {
        while (true) {
            String price_string = managerInputOutput.readLineIO("Введите price : ").trim().replace(',', '.');
            if (price_string.isEmpty()) {
                managerInputOutput.writeLineIO("Price не может быть пустым\n");
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(price_string);
                if (price.compareTo(BigDecimal.ZERO) >= 0) return price;
                managerInputOutput.writeLineIO("Price не может быть отрицательным\n");
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка при вводе формата price\n", Colors.RED);
            }
        }
    }
}