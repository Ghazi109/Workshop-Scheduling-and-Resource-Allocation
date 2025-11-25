package  data;

public class ActivityTypeConverter {
    public static int convertToInt(ActivityType type) {
        switch (type) {
            case TRANSFORMATION:
                return 0;
            case PAINT:
                return 1;
            case STICKER:
                return 2;
            case CHECK:
                return 3;
            default:
                throw new IllegalArgumentException("Type d'activité non pris en charge : " + type);
        }
    }
}