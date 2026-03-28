package model;

public class TimeSlot {

    private int id;

    private int startMinutes;

    private int patternId;

    private String dayPattern;

    private int durationMinutes;

    private boolean isOnlineSlot; // ← الجديد


    public TimeSlot(
            int id,
            int startMinutes,
            int patternId,
            int durationMinutes,
            boolean isOnlineSlot) {

        this.id = id;

        this.startMinutes =
                startMinutes;

        this.patternId =
                patternId;

        this.durationMinutes =
                durationMinutes;

        this.isOnlineSlot =
                isOnlineSlot;

        if (patternId==0){
            dayPattern = "TTS";
        }
        else {
            dayPattern = "MWT";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(int startMinutes) {
        this.startMinutes = startMinutes;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public String getDayPattern() {
        return dayPattern;
    }

    public void setDayPattern(String dayPattern) {
        this.dayPattern = dayPattern;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean getIsOnlineSlot() {
        return isOnlineSlot;
    }

    public void setOnlineSlot(boolean onlineSlot) {
        isOnlineSlot = onlineSlot;
    }

    public String convertMinutesToHour(int minutes){
        int hour =
                startMinutes / 60;

        int minute =
                startMinutes % 60;

        return "Slot "
                + id
                + " | "
                + String.format(
                "%02d:%02d",
                hour,
                minute);
    }
    // تحويل للدقائق → ساعة:دقيقة
    @Override
    public String toString() {

        return convertMinutesToHour(startMinutes)
                + " | Pattern "
                + patternId;
    }
}