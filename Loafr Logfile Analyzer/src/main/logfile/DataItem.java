package main.logfile;

public class DataItem {

    private String dataName, value;

    public DataItem(String dataName, String value) {
        this.dataName = dataName;
        this.value = value;
    }

    public String getName() { return dataName; }
    public String getValue() { return value; }

}
