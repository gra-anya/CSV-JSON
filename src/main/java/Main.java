import com.google.gson.*;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //CSV to JSON
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> employeesCSV = parseCSV(columnMapping, fileName);
        writeString(listToJson(employeesCSV));

        //XML to JSON
        List<Employee> employeesXML = parseXML("data.xml");
        writeString(listToJson(employeesXML));

        //JSON to Employee
        String json = readString("data.json");
        List<Employee> employees = jsonToList(json);
        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> employees = new ArrayList<>();
        JsonArray jsonElements = (JsonArray) JsonParser.parseString(json);
        Gson gson = new GsonBuilder().create();
        for (Object jsonObject : jsonElements) {
            Employee employee = gson.fromJson((JsonObject) jsonObject, Employee.class);
            employees.add(employee);
        }
        return employees;
    }

    public static String readString(String fileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.ready()) {
                builder.append(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> employees = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(fileName));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("employee");
            for (int i = 0; i < nodeList.getLength(); i++) {
                employees.add(getEmployee(nodeList.item(i)));
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return employees;
    }

    private static Employee getEmployee(Node node) {
        Employee employee = null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            long id = Long.parseLong(getTagValue("id", element));
            String firstName = getTagValue("firstName", element);
            String lastName = getTagValue("lastName", element);
            String country = getTagValue("country", element);
            int age = Integer.parseInt(getTagValue("age", element));
            employee = new Employee(id, firstName, lastName, country, age);
        }
        return employee;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> employees = null;
        ColumnPositionMappingStrategy<Employee> cpmStrategy = new ColumnPositionMappingStrategy<>();
        cpmStrategy.setType(Employee.class);
        cpmStrategy.setColumnMapping(columnMapping);

        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            CsvToBean<Employee> csvToBean = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(cpmStrategy)
                    .build();
            employees = csvToBean.parse();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(list);
    }

    public static void writeString(String json) {
        try (FileWriter fileWriter = new FileWriter("data.json")) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
