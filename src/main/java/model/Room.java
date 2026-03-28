package model;

public class Room {

    private int id;

    private String code;

    private boolean isLab;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getIsLab() {
        return isLab;
    }

    public void setIsLab(boolean lab) {
        isLab = lab;
    }

    public Room(
            int id,
            String code,
            boolean isLab) {

        this.id = id;
        this.code = code;
        this.isLab = isLab;
    }

    @Override
    public String toString() {

        return id
                + " - "
                + code
                + " - Lab: "
                + isLab;
    }
}