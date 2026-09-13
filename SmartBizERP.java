import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class SmartBizERP extends Application {

    // ============================================================
    // THEME CONSTANTS
    // ============================================================
    private boolean darkMode = false;

    // Light theme
    private static final String L_BG        = "#F0F2F5";
    private static final String L_SIDEBAR   = "#1A2236";
    private static final String L_CARD      = "#FFFFFF";
    private static final String L_TEXT      = "#1A2236";
    private static final String L_SUB       = "#6B7280";
    private static final String L_BORDER    = "#E5E7EB";
    private static final String L_HEADER    = "#FFFFFF";

    // Dark theme
    private static final String D_BG        = "#0F1117";
    private static final String D_SIDEBAR   = "#0B0E14";
    private static final String D_CARD      = "#1A1F2E";
    private static final String D_TEXT      = "#F1F5F9";
    private static final String D_SUB       = "#94A3B8";
    private static final String D_BORDER    = "#2D3748";
    private static final String D_HEADER    = "#141824";

    // Accent colors (constant)
    private static final String ACCENT      = "#3B82F6";
    private static final String ACCENT_DARK = "#2563EB";
    private static final String SUCCESS     = "#10B981";
    private static final String WARNING     = "#F59E0B";
    private static final String DANGER      = "#EF4444";
    private static final String PURPLE      = "#8B5CF6";
    private static final String ORANGE      = "#F97316";
    private static final String TEAL        = "#14B8A6";

    // ============================================================
    // APPLICATION STATE
    // ============================================================
    private BorderPane rootLayout;
    private VBox sidebarNav;
    private StackPane contentArea;
    private Label headerTitle;
    private Label breadcrumb;
    private Label notifBadge;
    private Label clockLabel;
    private Label dateLabel;
    private String currentModule = "Dashboard";
    private Stage primaryStage;
    private static final java.nio.file.Path DATA_FILE = Paths.get("data", "smartbiz-data.txt");

    // Dashboard KPI labels
    private Label kpiEmployees, kpiPresent, kpiSales, kpiExpenses;
    private Label kpiInventory, kpiPending, kpiPayroll, kpiLowStock;
    private Label kpiEmployeesTrend, kpiPresentTrend, kpiSalesTrend, kpiExpensesTrend;

    // Charts
    private LineChart<String, Number> salesLineChart;
    private PieChart categoryPieChart;
    private BarChart<String, Number> inventoryBarChart;
    private BarChart<String, Number> attendanceBarChart;
    private VBox activityFeed;

    // Sidebar nav buttons
    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    // ============================================================
    // DATA MODELS (inner static classes)
    // ============================================================

    public static class Employee {
        static int counter = 100;
        StringProperty id, name, fatherName, email, phone, department, position, joiningDate, status;
        DoubleProperty salary;
        public Employee(String name, String fatherName, String email, String phone,
                        String dept, String pos, String date, double salary, String status) {
            this.id          = new SimpleStringProperty("EMP-" + (++counter));
            this.name        = new SimpleStringProperty(name);
            this.fatherName  = new SimpleStringProperty(fatherName);
            this.email       = new SimpleStringProperty(email);
            this.phone       = new SimpleStringProperty(phone);
            this.department  = new SimpleStringProperty(dept);
            this.position    = new SimpleStringProperty(pos);
            this.joiningDate = new SimpleStringProperty(date);
            this.salary      = new SimpleDoubleProperty(salary);
            this.status      = new SimpleStringProperty(status);
        }
        public String getId()          { return id.get(); }
        public String getName()        { return name.get(); }
        public String getFatherName()  { return fatherName.get(); }
        public String getEmail()       { return email.get(); }
        public String getPhone()       { return phone.get(); }
        public String getDepartment()  { return department.get(); }
        public String getPosition()    { return position.get(); }
        public String getJoiningDate() { return joiningDate.get(); }
        public double getSalary()      { return salary.get(); }
        public String getStatus()      { return status.get(); }
        public void setName(String v)       { name.set(v); }
        public void setFatherName(String v) { fatherName.set(v); }
        public void setEmail(String v)      { email.set(v); }
        public void setPhone(String v)      { phone.set(v); }
        public void setDepartment(String v) { department.set(v); }
        public void setPosition(String v)   { position.set(v); }
        public void setJoiningDate(String v){ joiningDate.set(v); }
        public void setSalary(double v)     { salary.set(v); }
        public void setStatus(String v)     { status.set(v); }
    }

    public static class Department {
        static int counter = 0;
        StringProperty id, name, manager, description;
        IntegerProperty employeeCount;
        public Department(String name, String manager, String desc) {
            this.id            = new SimpleStringProperty("DEP-" + String.format("%02d", ++counter));
            this.name          = new SimpleStringProperty(name);
            this.manager       = new SimpleStringProperty(manager);
            this.description   = new SimpleStringProperty(desc);
            this.employeeCount = new SimpleIntegerProperty(0);
        }
        public String getId()          { return id.get(); }
        public String getName()        { return name.get(); }
        public String getManager()     { return manager.get(); }
        public String getDescription() { return description.get(); }
        public int getEmployeeCount()  { return employeeCount.get(); }
        public void setName(String v)        { name.set(v); }
        public void setManager(String v)     { manager.set(v); }
        public void setDescription(String v) { description.set(v); }
        public void setEmployeeCount(int v)  { employeeCount.set(v); }
    }

    public static class AttendanceRecord {
        static int counter = 0;
        StringProperty id, employeeId, employeeName, date, checkIn, checkOut, status;
        public AttendanceRecord(String empId, String empName, String date,
                                String checkIn, String checkOut, String status) {
            this.id           = new SimpleStringProperty("ATT-" + String.format("%04d", ++counter));
            this.employeeId   = new SimpleStringProperty(empId);
            this.employeeName = new SimpleStringProperty(empName);
            this.date         = new SimpleStringProperty(date);
            this.checkIn      = new SimpleStringProperty(checkIn);
            this.checkOut     = new SimpleStringProperty(checkOut);
            this.status       = new SimpleStringProperty(status);
        }
        public String getId()           { return id.get(); }
        public String getEmployeeId()   { return employeeId.get(); }
        public String getEmployeeName() { return employeeName.get(); }
        public String getDate()         { return date.get(); }
        public String getCheckIn()      { return checkIn.get(); }
        public String getCheckOut()     { return checkOut.get(); }
        public String getStatus()       { return status.get(); }
        public void setCheckOut(String v) { checkOut.set(v); }
        public void setStatus(String v)   { status.set(v); }
    }

    public static class Category {
        static int counter = 0;
        StringProperty id, name, description;
        IntegerProperty productCount;
        public Category(String name, String desc) {
            this.id           = new SimpleStringProperty("CAT-" + String.format("%02d", ++counter));
            this.name         = new SimpleStringProperty(name);
            this.description  = new SimpleStringProperty(desc);
            this.productCount = new SimpleIntegerProperty(0);
        }
        public String getId()          { return id.get(); }
        public String getName()        { return name.get(); }
        public String getDescription() { return description.get(); }
        public int getProductCount()   { return productCount.get(); }
        public void setName(String v)        { name.set(v); }
        public void setDescription(String v) { description.set(v); }
        public void setProductCount(int v)   { productCount.set(v); }
    }

    public static class Supplier {
        static int counter = 0;
        StringProperty id, name, company, phone, email, address, paymentStatus;
        public Supplier(String name, String company, String phone, String email, String address, String pStatus) {
            this.id            = new SimpleStringProperty("SUP-" + String.format("%03d", ++counter));
            this.name          = new SimpleStringProperty(name);
            this.company       = new SimpleStringProperty(company);
            this.phone         = new SimpleStringProperty(phone);
            this.email         = new SimpleStringProperty(email);
            this.address       = new SimpleStringProperty(address);
            this.paymentStatus = new SimpleStringProperty(pStatus);
        }
        public String getId()            { return id.get(); }
        public String getName()          { return name.get(); }
        public String getCompany()       { return company.get(); }
        public String getPhone()         { return phone.get(); }
        public String getEmail()         { return email.get(); }
        public String getAddress()       { return address.get(); }
        public String getPaymentStatus() { return paymentStatus.get(); }
        public void setName(String v)          { name.set(v); }
        public void setCompany(String v)       { company.set(v); }
        public void setPhone(String v)         { phone.set(v); }
        public void setEmail(String v)         { email.set(v); }
        public void setAddress(String v)       { address.set(v); }
        public void setPaymentStatus(String v) { paymentStatus.set(v); }
    }

    public static class Product {
        static int counter = 1000;
        StringProperty id, name, category, sku, supplier, status;
        DoubleProperty purchasePrice, sellingPrice;
        IntegerProperty quantity, minStock;
        public Product(String name, String cat, String sku, String supplier,
                       double pp, double sp, int qty, int minStock) {
            this.id            = new SimpleStringProperty("PRD-" + (++counter));
            this.name          = new SimpleStringProperty(name);
            this.category      = new SimpleStringProperty(cat);
            this.sku           = new SimpleStringProperty(sku);
            this.supplier      = new SimpleStringProperty(supplier);
            this.purchasePrice = new SimpleDoubleProperty(pp);
            this.sellingPrice  = new SimpleDoubleProperty(sp);
            this.quantity      = new SimpleIntegerProperty(qty);
            this.minStock      = new SimpleIntegerProperty(minStock);
            this.status        = new SimpleStringProperty(calcStatus(qty, minStock));
        }
        static String calcStatus(int qty, int min) {
            if (qty == 0)     return "OUT OF STOCK";
            if (qty <= min)   return "LOW STOCK";
            return "IN STOCK";
        }
        public String getId()            { return id.get(); }
        public String getName()          { return name.get(); }
        public String getCategory()      { return category.get(); }
        public String getSku()           { return sku.get(); }
        public String getSupplier()      { return supplier.get(); }
        public double getPurchasePrice() { return purchasePrice.get(); }
        public double getSellingPrice()  { return sellingPrice.get(); }
        public int getQuantity()         { return quantity.get(); }
        public int getMinStock()         { return minStock.get(); }
        public String getStatus()        { return status.get(); }
        public void setName(String v)           { name.set(v); }
        public void setCategory(String v)       { category.set(v); }
        public void setSku(String v)            { sku.set(v); }
        public void setSupplier(String v)       { supplier.set(v); }
        public void setPurchasePrice(double v)  { purchasePrice.set(v); }
        public void setSellingPrice(double v)   { sellingPrice.set(v); }
        public void setQuantity(int v)          { quantity.set(v); status.set(calcStatus(v, minStock.get())); }
        public void setMinStock(int v)          { minStock.set(v); }
    }

    public static class SaleItem {
        String productId, productName;
        int quantity;
        double unitPrice, total;
        public SaleItem(String pid, String pname, int qty, double price) {
            this.productId = pid; this.productName = pname;
            this.quantity = qty; this.unitPrice = price;
            this.total = qty * price;
        }
    }

    public static class Sale {
        static int counter = 1041;
        StringProperty id, customer, date, status;
        DoubleProperty subtotal, discount, tax, grandTotal;
        List<SaleItem> items;
        public Sale(String customer, List<SaleItem> items, double discount, double taxRate, String date) {
            this.id        = new SimpleStringProperty("INV-" + (++counter));
            this.customer  = new SimpleStringProperty(customer);
            this.date      = new SimpleStringProperty(date);
            this.status    = new SimpleStringProperty("Paid");
            this.items     = items;
            double sub     = items.stream().mapToDouble(i -> i.total).sum();
            double disc    = discount;
            double taxAmt  = (sub - disc) * taxRate / 100.0;
            this.subtotal  = new SimpleDoubleProperty(sub);
            this.discount  = new SimpleDoubleProperty(disc);
            this.tax       = new SimpleDoubleProperty(taxAmt);
            this.grandTotal= new SimpleDoubleProperty(sub - disc + taxAmt);
        }
        public String getId()         { return id.get(); }
        public String getCustomer()   { return customer.get(); }
        public String getDate()       { return date.get(); }
        public String getStatus()     { return status.get(); }
        public double getSubtotal()   { return subtotal.get(); }
        public double getDiscount()   { return discount.get(); }
        public double getTax()        { return tax.get(); }
        public double getGrandTotal() { return grandTotal.get(); }
    }

    public static class PurchaseOrder {
        static int counter = 2000;
        StringProperty id, supplier, product, date, status;
        IntegerProperty quantity;
        DoubleProperty unitPrice, total;
        String productId;
        public PurchaseOrder(String supplier, String productId, String product,
                             int qty, double unitPrice, String date) {
            this.id        = new SimpleStringProperty("PO-" + (++counter));
            this.supplier  = new SimpleStringProperty(supplier);
            this.productId = productId;
            this.product   = new SimpleStringProperty(product);
            this.quantity  = new SimpleIntegerProperty(qty);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.total     = new SimpleDoubleProperty(qty * unitPrice);
            this.date      = new SimpleStringProperty(date);
            this.status    = new SimpleStringProperty("Pending");
        }
        public String getId()        { return id.get(); }
        public String getSupplier()  { return supplier.get(); }
        public String getProduct()   { return product.get(); }
        public int getQuantity()     { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getTotal()     { return total.get(); }
        public String getDate()      { return date.get(); }
        public String getStatus()    { return status.get(); }
        public void setStatus(String v) { status.set(v); }
    }

    public static class Expense {
        static int counter = 3000;
        StringProperty id, title, category, date, description, paymentMethod;
        DoubleProperty amount;
        public Expense(String title, String cat, double amount, String date, String desc, String method) {
            this.id            = new SimpleStringProperty("EXP-" + (++counter));
            this.title         = new SimpleStringProperty(title);
            this.category      = new SimpleStringProperty(cat);
            this.amount        = new SimpleDoubleProperty(amount);
            this.date          = new SimpleStringProperty(date);
            this.description   = new SimpleStringProperty(desc);
            this.paymentMethod = new SimpleStringProperty(method);
        }
        public String getId()            { return id.get(); }
        public String getTitle()         { return title.get(); }
        public String getCategory()      { return category.get(); }
        public double getAmount()        { return amount.get(); }
        public String getDate()          { return date.get(); }
        public String getDescription()   { return description.get(); }
        public String getPaymentMethod() { return paymentMethod.get(); }
        public void setTitle(String v)         { title.set(v); }
        public void setCategory(String v)      { category.set(v); }
        public void setAmount(double v)        { amount.set(v); }
        public void setDate(String v)          { date.set(v); }
        public void setDescription(String v)   { description.set(v); }
        public void setPaymentMethod(String v) { paymentMethod.set(v); }
    }

    public static class PayrollRecord {
        static int counter = 0;
        StringProperty id, employeeId, employeeName, department, month, paymentStatus, paymentDate;
        DoubleProperty basicSalary, bonus, tax, deductions, netSalary;
        public PayrollRecord(String empId, String empName, String dept, String month,
                             double basic, double bonus, double tax, double deductions) {
            this.id            = new SimpleStringProperty("PAY-" + String.format("%04d", ++counter));
            this.employeeId    = new SimpleStringProperty(empId);
            this.employeeName  = new SimpleStringProperty(empName);
            this.department    = new SimpleStringProperty(dept);
            this.month         = new SimpleStringProperty(month);
            this.basicSalary   = new SimpleDoubleProperty(basic);
            this.bonus         = new SimpleDoubleProperty(bonus);
            this.tax           = new SimpleDoubleProperty(tax);
            this.deductions    = new SimpleDoubleProperty(deductions);
            this.netSalary     = new SimpleDoubleProperty(basic + bonus - tax - deductions);
            this.paymentStatus = new SimpleStringProperty("Pending");
            this.paymentDate   = new SimpleStringProperty("-");
        }
        public String getId()             { return id.get(); }
        public String getEmployeeId()     { return employeeId.get(); }
        public String getEmployeeName()   { return employeeName.get(); }
        public String getDepartment()     { return department.get(); }
        public String getMonth()          { return month.get(); }
        public double getBasicSalary()    { return basicSalary.get(); }
        public double getBonus()          { return bonus.get(); }
        public double getTax()            { return tax.get(); }
        public double getDeductions()     { return deductions.get(); }
        public double getNetSalary()      { return netSalary.get(); }
        public String getPaymentStatus()  { return paymentStatus.get(); }
        public String getPaymentDate()    { return paymentDate.get(); }
        public void setBasicSalary(double v)  { basicSalary.set(v); recalc(); }
        public void setBonus(double v)        { bonus.set(v); recalc(); }
        public void setTax(double v)          { tax.set(v); recalc(); }
        public void setDeductions(double v)   { deductions.set(v); recalc(); }
        public void setPaymentStatus(String v){ paymentStatus.set(v); }
        public void setPaymentDate(String v)  { paymentDate.set(v); }
        private void recalc() { netSalary.set(basicSalary.get() + bonus.get() - tax.get() - deductions.get()); }
    }

    public static class Notification {
        static int counter = 0;
        String id, type, title, message, time;
        boolean read;
        public Notification(String type, String title, String message) {
            this.id      = "N" + (++counter);
            this.type    = type;
            this.title   = title;
            this.message = message;
            this.time    = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
            this.read    = false;
        }
    }

    // ============================================================
    // OBSERVABLE DATA COLLECTIONS
    // ============================================================
    private final ObservableList<Employee>        employees     = FXCollections.observableArrayList();
    private final ObservableList<Department>      departments   = FXCollections.observableArrayList();
    private final ObservableList<AttendanceRecord>attendance    = FXCollections.observableArrayList();
    private final ObservableList<Category>        categories    = FXCollections.observableArrayList();
    private final ObservableList<Supplier>        suppliers     = FXCollections.observableArrayList();
    private final ObservableList<Product>         products      = FXCollections.observableArrayList();
    private final ObservableList<Sale>            sales         = FXCollections.observableArrayList();
    private final ObservableList<PurchaseOrder>   purchases     = FXCollections.observableArrayList();
    private final ObservableList<Expense>         expenses      = FXCollections.observableArrayList();
    private final ObservableList<PayrollRecord>   payrolls      = FXCollections.observableArrayList();
    private final ObservableList<Notification>    notifications = FXCollections.observableArrayList();
    private final ObservableList<String>          activities    = FXCollections.observableArrayList();

    // Company settings
    private String companyName = "TechVision Solutions (Pvt) Ltd";
    private String ownerName   = "Muhammad Umar";
    private String companyEmail= "info@techvision.pk";
    private String companyPhone= "+92-300-1234567";
    private String companyAddr = "Main Boulevard, Okara, Punjab, Pakistan";
    private String currency    = "Rs.";

    // Cart for POS
    private final ObservableList<SaleItem> cart = FXCollections.observableArrayList();

    // ============================================================
    // SAMPLE DATA INITIALIZATION
    // ============================================================
    private void loadSampleData() {
        // Departments
        String[][] depts = {
            {"Information Technology", "Umar Farooq", "Handles all IT infrastructure and software"},
            {"Human Resources",        "Ayesha Malik", "Manages recruitment and employee relations"},
            {"Finance & Accounts",     "Hassan Raza",  "Handles accounting, payroll and budgeting"},
            {"Sales & Marketing",      "Sara Ahmed",   "Drives revenue and brand awareness"},
            {"Operations",             "Tariq Mehmood","Manages daily business operations"},
            {"Management",             "Rao Umar",     "Executive leadership and strategy"}
        };
        for (String[] d : depts) departments.add(new Department(d[0], d[1], d[2]));

        // Categories
        String[][] cats = {
            {"Electronics",    "Electronic devices and accessories"},
            {"Office Supplies","Stationery and office consumables"},
            {"Furniture",      "Office and workspace furniture"},
            {"Software",       "Software licenses and subscriptions"},
            {"Accessories",    "Computer and mobile accessories"},
            {"Networking",     "Network equipment and cables"}
        };
        for (String[] c : cats) categories.add(new Category(c[0], c[1]));

        // Suppliers
        String[][] sups = {
            {"Ali Hassan",    "TechMart Pakistan",    "0300-1234567", "ali@techmart.pk",   "Lahore, Punjab",     "Paid"},
            {"Bilal Ahmed",   "Office World",         "0321-9876543", "bilal@offworld.pk", "Karachi, Sindh",     "Pending"},
            {"Zainab Raza",   "FurniCorp PK",         "0333-5678901", "zainab@furni.pk",   "Islamabad, ICT",     "Paid"},
            {"Kamran Sheikh", "NetSolutions",          "0345-2345678", "kamran@netsl.pk",   "Faisalabad, Punjab", "Pending"},
            {"Nadia Malik",   "Accessory Hub",        "0311-3456789", "nadia@acchub.pk",   "Multan, Punjab",     "Paid"}
        };
        for (String[] s : sups) suppliers.add(new Supplier(s[0], s[1], s[2], s[3], s[4], s[5]));

        // Employees
        Object[][] emps = {
            {"Ahmed Ali",        "Muhammad Ali",       "ahmed@techvision.pk",   "0300-1111111", "Information Technology", "Senior Developer",    "01-Jan-2022",  85000, "Active"},
            {"Sara Khan",        "Imran Khan",         "sara@techvision.pk",    "0300-2222222", "Human Resources",        "HR Manager",          "15-Mar-2021", 70000, "Active"},
            {"Hassan Raza",      "Raza Ullah",         "hassan@techvision.pk",  "0300-3333333", "Finance & Accounts",     "Accountant",          "10-Jun-2020",  65000, "Active"},
            {"Ayesha Malik",     "Malik Rafiq",        "ayesha@techvision.pk",  "0300-4444444", "Sales & Marketing",      "Sales Executive",     "20-Aug-2022",  55000, "Active"},
            {"Usman Tariq",      "Tariq Hussain",      "usman@techvision.pk",   "0300-5555555", "Information Technology", "Junior Developer",    "05-Feb-2023",  45000, "Active"},
            {"Fatima Zahra",     "Zaheer Ahmed",       "fatima@techvision.pk",  "0300-6666666", "Operations",             "Operations Manager",  "12-Nov-2019",  90000, "Active"},
            {"Bilal Hussain",    "Hussain Khan",       "bilal@techvision.pk",   "0300-7777777", "Sales & Marketing",      "Marketing Analyst",   "08-Apr-2022",  52000, "Active"},
            {"Zara Ahmed",       "Ahmed Siddiqui",     "zara@techvision.pk",    "0300-8888888", "Human Resources",        "HR Executive",        "25-Sep-2023",  42000, "Active"},
            {"Kamran Sheikh",    "Sheikh Azhar",       "kamran@techvision.pk",  "0300-9999999", "Information Technology", "DevOps Engineer",     "14-Jul-2021",  80000, "Active"},
            {"Nadia Parveen",    "Parveen Akhtar",     "nadia@techvision.pk",   "0301-1234567", "Finance & Accounts",     "Finance Officer",     "30-Jan-2022",  58000, "On Leave"}
        };
        for (Object[] e : emps)
            employees.add(new Employee((String)e[0],(String)e[1],(String)e[2],(String)e[3],
                                       (String)e[4],(String)e[5],(String)e[6],(double)(int)e[7],(String)e[8]));
        updateDeptCounts();

        // Products
        Object[][] prods = {
            {"Dell Laptop 15\"",       "Electronics",    "DL-001", "TechMart Pakistan",    95000, 115000,  8,  3},
            {"HP LaserJet Pro",        "Electronics",    "HJ-002", "TechMart Pakistan",    35000,  42000,  5,  2},
            {"Office Chair",           "Furniture",      "OC-003", "FurniCorp PK",          8500,  12000, 12,  4},
            {"Executive Desk",         "Furniture",      "ED-004", "FurniCorp PK",         22000,  28000,  6,  2},
            {"Wireless Mouse",         "Accessories",    "WM-005", "Accessory Hub",         1500,   2200, 25, 10},
            {"Mechanical Keyboard",    "Accessories",    "MK-006", "Accessory Hub",         4500,   6500, 18,  5},
            {"24\" Monitor",           "Electronics",    "MN-007", "TechMart Pakistan",    28000,  35000,  7,  3},
            {"A4 Paper Ream",          "Office Supplies","AP-008", "Office World",            800,   1200, 50, 15},
            {"Stapler Set",            "Office Supplies","SS-009", "Office World",            350,    600, 30, 10},
            {"Network Switch 24-Port", "Networking",     "NS-010", "NetSolutions",          12000,  16000,  4,  2},
            {"CAT6 Cable (100m)",      "Networking",     "CC-011", "NetSolutions",           5500,   7500, 10,  3},
            {"USB Hub 7-Port",         "Accessories",    "UH-012", "Accessory Hub",         1200,   1800, 20,  8},
            {"Microsoft Office 2024",  "Software",       "MS-013", "TechMart Pakistan",    18000,  22000,  2,  1},
            {"Antivirus License",      "Software",       "AV-014", "TechMart Pakistan",     3500,   5000, 15,  5},
            {"Webcam HD 1080p",        "Electronics",    "WC-015", "TechMart Pakistan",     4500,   6500,  0,  3}
        };
        for (Object[] p : prods)
            products.add(new Product((String)p[0],(String)p[1],(String)p[2],(String)p[3],
                                     (double)(int)p[4],(double)(int)p[5],(int)p[6],(int)p[7]));
        updateCatCounts();

        // Sales
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String[] custNames = {"Zaheer Traders","Ali Enterprises","Khan & Sons","Tech Park Ltd","City Electronics",
                              "Global Systems","Pak Office Hub","Sunrise Corp","Digital Solutions","Fast Forward Ltd"};
        for (int i = 0; i < 10; i++) {
            List<SaleItem> items = new ArrayList<>();
            Product p1 = products.get(i % products.size());
            Product p2 = products.get((i + 2) % products.size());
            items.add(new SaleItem(p1.getId(), p1.getName(), (i % 3) + 1, p1.getSellingPrice()));
            items.add(new SaleItem(p2.getId(), p2.getName(), (i % 2) + 1, p2.getSellingPrice()));
            Sale s = new Sale(custNames[i], items, 500 * (i + 1), 5,
                              LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            sales.add(s);
        }

        // Expenses
        String[][] exps = {
            {"Office Rent",           "Rent",       "75000",  "Utilities",  "Monthly office rent",           "Bank Transfer"},
            {"Electricity Bill",      "Utilities",  "18000",  "",           "LESCO monthly bill",            "Online"},
            {"Internet Connection",   "Utilities",   "8500",  "",           "Fiber broadband monthly",       "Bank Transfer"},
            {"Marketing Campaign",    "Marketing",  "35000",  "",           "Facebook & Google Ads",         "Online"},
            {"Office Stationery",     "Office",      "5500",  "",           "Pens, paper, files",            "Cash"},
            {"Vehicle Fuel",          "Transport",  "12000",  "",           "Company vehicle fuel",          "Cash"},
            {"Equipment Maintenance", "Maintenance","15000",  "",           "AC and computer maintenance",   "Bank Transfer"},
            {"Team Lunch",            "Other",       "9500",  "",           "Monthly team lunch",            "Cash"},
            {"Software Subscription", "Office",     "22000",  "",           "Cloud software licenses",       "Online"},
            {"Training Workshop",     "Other",      "28000",  "",           "Employee training session",     "Bank Transfer"}
        };
        String[] expDates = {"01","02","03","05","08","10","12","15","18","20"};
        for (int i = 0; i < exps.length; i++) {
            String date = expDates[i] + "-Sep-2026";
            expenses.add(new Expense(exps[i][0], exps[i][1],
                                     Double.parseDouble(exps[i][2]), date, exps[i][4], exps[i][5]));
        }

        // Attendance (today)
        String[] presentEmps = {"Ahmed Ali","Sara Khan","Hassan Raza","Ayesha Malik","Usman Tariq",
                                 "Fatima Zahra","Bilal Hussain","Kamran Sheikh"};
        String[] times = {"08:55","09:02","08:48","09:15","09:30","08:40","09:05","08:50"};
        for (int i = 0; i < presentEmps.length; i++) {
            String employeeName = presentEmps[i];
            Optional<Employee> emp = employees.stream().filter(e -> e.getName().equals(employeeName)).findFirst();
            if (emp.isPresent()) {
                attendance.add(new AttendanceRecord(emp.get().getId(), employeeName, today,
                                                    times[i] + " AM", "17:00 PM", "Present"));
            }
        }
        // Absent
        attendance.add(new AttendanceRecord("EMP-109","Zara Ahmed", today, "-", "-", "Absent"));

        // Payroll
        for (Employee emp : employees) {
            double basic = emp.getSalary();
            double bonus = basic * 0.1;
            double tax   = basic * 0.05;
            double dedu  = 1000;
            PayrollRecord pr = new PayrollRecord(emp.getId(), emp.getName(), emp.getDepartment(),
                                                 "September 2026", basic, bonus, tax, dedu);
            if (Math.random() > 0.5) {
                pr.setPaymentStatus("Paid");
                pr.setPaymentDate("05-Sep-2026");
            }
            payrolls.add(pr);
        }

        // Purchase orders
        String[] poStatuses = {"Pending","Approved","Received","Cancelled","Pending"};
        for (int i = 0; i < 5; i++) {
            Product p = products.get(i * 2);
            PurchaseOrder po = new PurchaseOrder(suppliers.get(i % suppliers.size()).getName(),
                                                 p.getId(), p.getName(), 10 + i * 2,
                                                 p.getPurchasePrice(),
                                                 LocalDate.now().minusDays(i * 2).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            po.setStatus(poStatuses[i]);
            purchases.add(po);
        }

        // Initial notifications
        notifications.add(new Notification("warning", "Low Stock Alert",
            "Webcam HD 1080p is out of stock. Please reorder."));
        notifications.add(new Notification("info", "Payroll Due",
            "5 employee salaries are pending for September 2026."));
        notifications.add(new Notification("success", "Purchase Received",
            "PO-2002 has been received and stock updated."));

        // Activities
        activities.add("● Invoice #INV-1042 created for Zaheer Traders  •  2 min ago");
        activities.add("● Ahmed Ali checked in at 08:55 AM  •  8 min ago");
        activities.add("● Product 'Wireless Mouse' stock updated  •  15 min ago");
        activities.add("● Salary processed for September 2026  •  1 hour ago");
        activities.add("● New supplier 'Accessory Hub' added  •  3 hours ago");
    }

    private String cleanText(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\r", " ").replace("\n", " ");
    }

    private void record(StringBuilder data, String section, Object... values) {
        data.append(section);
        for (Object value : values) data.append('\t').append(cleanText(String.valueOf(value)));
        data.append('\n');
    }

    private void saveDataToTxt() {
        StringBuilder data = new StringBuilder();
        data.append("SmartBiz ERP data export\n");
        data.append("Generated\t").append(LocalDateTime.now()).append("\n\n");
        record(data, "COMPANY", companyName, ownerName, companyEmail, companyPhone, companyAddr, currency);

        for (Department d : departments)
            record(data, "DEPARTMENT", d.getId(), d.getName(), d.getManager(), d.getDescription(), d.getEmployeeCount());
        for (Employee e : employees)
            record(data, "EMPLOYEE", e.getId(), e.getName(), e.getFatherName(), e.getEmail(), e.getPhone(),
                   e.getDepartment(), e.getPosition(), e.getJoiningDate(), e.getSalary(), e.getStatus());
        for (AttendanceRecord a : attendance)
            record(data, "ATTENDANCE", a.getId(), a.getEmployeeId(), a.getEmployeeName(), a.getDate(),
                   a.getCheckIn(), a.getCheckOut(), a.getStatus());
        for (Category c : categories)
            record(data, "CATEGORY", c.getId(), c.getName(), c.getDescription(), c.getProductCount());
        for (Supplier s : suppliers)
            record(data, "SUPPLIER", s.getId(), s.getName(), s.getCompany(), s.getPhone(), s.getEmail(),
                   s.getAddress(), s.getPaymentStatus());
        for (Product p : products)
            record(data, "PRODUCT", p.getId(), p.getName(), p.getCategory(), p.getSku(), p.getSupplier(),
                   p.getPurchasePrice(), p.getSellingPrice(), p.getQuantity(), p.getMinStock(), p.getStatus());
        for (Sale s : sales)
            record(data, "SALE", s.getId(), s.getCustomer(), s.getDate(), s.getStatus(), s.getSubtotal(),
                   s.getDiscount(), s.getTax(), s.getGrandTotal(), s.items.size());
        for (PurchaseOrder p : purchases)
            record(data, "PURCHASE", p.getId(), p.getSupplier(), p.productId, p.getProduct(), p.getQuantity(),
                   p.getUnitPrice(), p.getTotal(), p.getDate(), p.getStatus());
        for (Expense e : expenses)
            record(data, "EXPENSE", e.getId(), e.getTitle(), e.getCategory(), e.getAmount(), e.getDate(),
                   e.getDescription(), e.getPaymentMethod());
        for (PayrollRecord p : payrolls)
            record(data, "PAYROLL", p.getId(), p.getEmployeeId(), p.getEmployeeName(), p.getDepartment(),
                   p.getMonth(), p.getBasicSalary(), p.getBonus(), p.getTax(), p.getDeductions(),
                   p.getNetSalary(), p.getPaymentStatus(), p.getPaymentDate());
        for (Notification n : notifications)
            record(data, "NOTIFICATION", n.id, n.type, n.title, n.message, n.time, n.read);
        for (String activity : activities) record(data, "ACTIVITY", activity);

        try {
            Files.createDirectories(DATA_FILE.getParent());
            Files.writeString(DATA_FILE, data.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Could not save ERP data: " + ex.getMessage());
        }
    }

    // ============================================================
    // UTILITY HELPERS
    // ============================================================
    private String fmt(double v) {
        return currency + " " + String.format("%,.0f", v);
    }

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    private void updateDeptCounts() {
        for (Department d : departments) {
            long cnt = employees.stream().filter(e -> e.getDepartment().equals(d.getName())).count();
            d.setEmployeeCount((int) cnt);
        }
    }

    private void updateCatCounts() {
        for (Category c : categories) {
            long cnt = products.stream().filter(p -> p.getCategory().equals(c.getName())).count();
            c.setProductCount((int) cnt);
        }
    }

    private void addActivity(String msg) {
        activities.add(0, "● " + msg + "  •  Just now");
        if (activities.size() > 20) activities.remove(activities.size() - 1);
        refreshActivityFeed();
    }

    private void addNotification(String type, String title, String msg) {
        notifications.add(0, new Notification(type, title, msg));
        updateNotifBadge();
    }

    private void updateNotifBadge() {
        long unread = notifications.stream().filter(n -> !n.read).count();
        Platform.runLater(() -> {
            if (notifBadge != null) {
                notifBadge.setText(String.valueOf(unread));
                notifBadge.setVisible(unread > 0);
            }
        });
    }

    // ============================================================
    // DASHBOARD KPI CALCULATIONS
    // ============================================================
    private int getTotalEmployees() { return employees.size(); }
    private int getPresentToday() {
        String td = today();
        return (int) attendance.stream().filter(a -> a.getDate().equals(td) &&
                (a.getStatus().equals("Present") || a.getStatus().equals("Checked In") || a.getStatus().equals("Checked Out"))).count();
    }
    private double getTotalSales()    { return sales.stream().mapToDouble(Sale::getGrandTotal).sum(); }
    private double getTotalExpenses() { return expenses.stream().mapToDouble(Expense::getAmount).sum(); }
    private double getInventoryValue(){ return products.stream().mapToDouble(p -> p.getSellingPrice() * p.getQuantity()).sum(); }
    private long getLowStockCount()   { return products.stream().filter(p -> p.getStatus().equals("LOW STOCK") || p.getStatus().equals("OUT OF STOCK")).count(); }
    private double getMonthlyPayroll(){ return payrolls.stream().mapToDouble(PayrollRecord::getNetSalary).sum(); }
    private long getPendingPayments() { return payrolls.stream().filter(p -> p.getPaymentStatus().equals("Pending")).count(); }

    private void refreshDashboardKPIs() {
        Platform.runLater(() -> {
            if (kpiEmployees == null) return;
            kpiEmployees.setText(String.valueOf(getTotalEmployees()));
            kpiPresent.setText(String.valueOf(getPresentToday()));
            kpiSales.setText(fmt(getTotalSales()));
            kpiExpenses.setText(fmt(getTotalExpenses()));
            kpiInventory.setText(fmt(getInventoryValue()));
            kpiPending.setText(String.valueOf(getPendingPayments()));
            kpiPayroll.setText(fmt(getMonthlyPayroll()));
            kpiLowStock.setText(String.valueOf(getLowStockCount()));
        });
    }

    // ============================================================
    // MAIN APPLICATION START
    // ============================================================
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        loadSampleData();

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + getBg() + ";");

        // Header
        Node header = buildHeader();
        rootLayout.setTop(header);

        // Sidebar
        Node sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + getBg() + ";");
        rootLayout.setCenter(contentArea);

        navigateTo("Dashboard");

        Scene scene = new Scene(rootLayout, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("SmartBiz ERP — Enterprise Resource Planning");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setOnCloseRequest(event -> saveDataToTxt());
        stage.show();

        startClock();
        updateNotifBadge();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            if (clockLabel != null) clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            if (dateLabel  != null) dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    // ============================================================
    // HEADER
    // ============================================================
    private Node buildHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + getHeader() + "; -fx-border-color: " + getBorder() + "; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(0, 0, 0, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMinHeight(60);
        header.setPrefHeight(60);

        // Brand block aligned with the sidebar, matching the reference layout.
        HBox brand = new HBox(10);
        brand.setPrefWidth(240);
        brand.setMinWidth(240);
        brand.setMaxWidth(240);
        brand.setPadding(new Insets(0, 18, 0, 20));
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setStyle("-fx-background-color: " + L_SIDEBAR + "; -fx-border-color: transparent " + ACCENT + " transparent transparent; -fx-border-width: 0 2 0 0;");

        Label logo = new Label("S");
        logo.setMinSize(40, 40);
        logo.setPrefSize(40, 40);
        logo.setAlignment(Pos.CENTER);
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; " +
            "-fx-background-color: " + ACCENT + "; -fx-background-radius: 8;");

        Label brandDivider = new Label("|");
        brandDivider.setStyle("-fx-font-size: 20px; -fx-text-fill: rgba(255,255,255,0.35);");
        Text brandName = new Text("SmartBiz ");
        brandName.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-fill: white;");
        Text brandProduct = new Text("ERP");
        brandProduct.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-fill: " + ACCENT + ";");
        TextFlow brandText = new TextFlow(brandName, brandProduct);
        brandText.setTextAlignment(TextAlignment.LEFT);
        brand.getChildren().addAll(logo, brandDivider, brandText);

        // Page title area
        HBox titleArea = new HBox(12);
        titleArea.setPadding(new Insets(0, 0, 0, 22));
        titleArea.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createWindowButton("‹", getSub());
        backButton.setMinSize(28, 34);
        backButton.setPrefSize(28, 34);
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getSub() + "; " +
            "-fx-font-size: 25px; -fx-font-weight: normal; -fx-padding: 0; -fx-cursor: hand;");
        backButton.setTooltip(new Tooltip("Return to Dashboard"));
        backButton.setOnAction(e -> navigateTo("Dashboard"));

        VBox titleText = new VBox(3);
        headerTitle = new Label("Dashboard");
        headerTitle.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");
        breadcrumb = new Label("Home > Dashboard");
        breadcrumb.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getSub() + ";");
        titleText.getChildren().addAll(headerTitle, breadcrumb);
        titleArea.getChildren().addAll(backButton, titleText);
        HBox.setHgrow(titleArea, Priority.ALWAYS);

        // Right side
        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(0, 12, 0, 10));

        // Notification bell
        StackPane notifBtn = new StackPane();
        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        notifBadge = new Label("0");
        notifBadge.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white; -fx-font-size: 9px; " +
                            "-fx-min-width: 16px; -fx-min-height: 16px; -fx-padding: 1px 3px; " +
                            "-fx-background-radius: 8px; -fx-font-weight: bold;");
        notifBadge.setVisible(false);
        StackPane.setAlignment(notifBadge, Pos.TOP_RIGHT);
        notifBtn.getChildren().addAll(bell, notifBadge);
        notifBtn.setOnMouseClicked(e -> navigateTo("Notifications"));
        notifBtn.setStyle("-fx-cursor: hand;");
        notifBtn.setMinSize(34, 34);
        Tooltip.install(notifBtn, new Tooltip("Notifications"));

        // User info
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER);
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 20px;");
        VBox userInfo = new VBox(0);
        Label userName = new Label("Administrator");
        userName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");
        Label userRole = new Label("Super Admin");
        userRole.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getSub() + ";");
        userInfo.getChildren().addAll(userName, userRole);
        userBox.getChildren().addAll(avatar, userInfo);
        Label userArrow = new Label("▾");
        userArrow.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + ";");
        userBox.getChildren().add(userArrow);
        userBox.setMinHeight(42);
        userBox.setPadding(new Insets(5, 10, 5, 9));
        userBox.setStyle("-fx-background-color: " + getBg() + "; -fx-background-radius: 8; " +
                "-fx-border-color: " + getBorder() + "; -fx-border-radius: 8; -fx-border-width: 1;");

        Button saveBtn = createWindowButton("Save", SUCCESS);
        saveBtn.setStyle("-fx-background-color: " + SUCCESS + "; -fx-text-fill: white; " +
                 "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7 13; " +
                 "-fx-background-radius: 6; -fx-cursor: hand;");
        saveBtn.setTooltip(new Tooltip("Save ERP data to the text file"));
        saveBtn.setOnAction(e -> {
            saveDataToTxt();
            showSuccess("Data Saved", "All ERP data was saved to data/smartbiz-data.txt.");
        });
        Button minimizeBtn = createWindowButton("—", getSub());
        minimizeBtn.setAccessibleText("Minimize window");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));
        Button maximizeBtn = createWindowButton("□", getSub());
        maximizeBtn.setAccessibleText("Maximize window");
        maximizeBtn.setOnAction(e -> primaryStage.setMaximized(!primaryStage.isMaximized()));
        Button closeBtn = createWindowButton("×", DANGER);
        closeBtn.setAccessibleText("Close window");
        closeBtn.setOnAction(e -> primaryStage.fireEvent(
                new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        HBox windowControls = new HBox(2, minimizeBtn, maximizeBtn, closeBtn);
        windowControls.setAlignment(Pos.CENTER);
        right.getChildren().addAll(notifBtn, saveBtn, new Separator(Orientation.VERTICAL), userBox,
                                   new Separator(Orientation.VERTICAL), windowControls);
        header.getChildren().addAll(brand, titleArea, right);
        return header;
    }

    private Button createWindowButton(String label, String color) {
        Button button = new Button(label);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5 9; " +
                        "-fx-min-width: 32px; -fx-min-height: 30px; " +
                        "-fx-background-radius: 4; -fx-cursor: hand;");
        return button;
    }

    // ============================================================
    // SIDEBAR
    // ============================================================
    private Node buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setStyle("-fx-background-color: #1A2236;");

        ScrollPane scroll = new ScrollPane(sidebar);
        scroll.setPrefWidth(240);
        scroll.setMinWidth(240);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1A2236; -fx-background-color: #1A2236; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        sidebarNav = sidebar;

        // Nav sections
        String[][] navItems = {
            {"Dashboard",      "📊"},
            {"─ MANAGEMENT ─", ""},
            {"Employees",      "👥"},
            {"Departments",    "🏢"},
            {"Attendance",     "📅"},
            {"─ INVENTORY ─",  ""},
            {"Inventory",      "📦"},
            {"Categories",     "🏷"},
            {"Suppliers",      "🚚"},
            {"─ FINANCE ─",    ""},
            {"Sales",          "💰"},
            {"Purchases",      "🛒"},
            {"Expenses",       "💸"},
            {"Payroll",        "💵"},
            {"─ ANALYTICS ─",  ""},
            {"Reports",        "📈"},
            {"Notifications",  "🔔"},
            {"Settings",       "⚙"}
        };

        for (String[] item : navItems) {
            if (item[0].startsWith("─")) {
                Label sep = new Label(item[0]);
                sep.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 9px; -fx-padding: 14 16 6 16; -fx-font-weight: bold;");
                sep.setMaxWidth(Double.MAX_VALUE);
                sidebar.getChildren().add(sep);
            } else {
                Button btn = createNavButton(item[1], item[0]);
                navButtons.put(item[0], btn);
                sidebar.getChildren().add(btn);
            }
        }

        // Bottom spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Version info
        Label version = new Label("SmartBiz ERP v2.0\n© 2026 TechVision");
        version.setStyle("-fx-text-fill: rgba(255,255,255,0.2); -fx-font-size: 9px; -fx-padding: 12 16 16 16; -fx-text-alignment: center;");
        sidebar.getChildren().add(version);

        return scroll;
    }

    private Button createNavButton(String icon, String label) {
        Button btn = new Button();
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 14px;");
        ico.setMinWidth(20);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px;");
        content.getChildren().addAll(ico, lbl);
        btn.setGraphic(content);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setCursor(Cursor.HAND);
        styleNavBtn(btn, false);
        btn.setOnAction(e -> navigateTo(label));
        return btn;
    }

    private void styleNavBtn(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: " + ACCENT + "22; -fx-text-fill: " + ACCENT + "; " +
                         "-fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 0 3; " +
                         "-fx-background-radius: 0; -fx-cursor: hand; -fx-font-size: 13px;");
            // Also style label inside
            HBox hb = (HBox) btn.getGraphic();
            for (Node n : hb.getChildren())
                ((Label)n).setStyle(((Label)n).getStyle() + "-fx-text-fill: " + ACCENT + ";");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                         "-fx-border-color: transparent; -fx-background-radius: 0; -fx-cursor: hand; -fx-font-size: 13px;");
            HBox hb = (HBox) btn.getGraphic();
            for (Node n : hb.getChildren())
                ((Label)n).setStyle(((Label)n).getStyle() + "-fx-text-fill: rgba(255,255,255,0.7);");
        }
    }

    private void navigateTo(String module) {
        currentModule = module;
        navButtons.forEach((name, btn) -> styleNavBtn(btn, name.equals(module)));
        if (headerTitle != null) headerTitle.setText(module);
        if (breadcrumb  != null) breadcrumb.setText("Home > " + module);

        contentArea.getChildren().clear();
        ScrollPane scroll = new ScrollPane(buildModule(module));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + getBg() + "; -fx-background: " + getBg() + "; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentArea.getChildren().add(scroll);
    }

    private Node buildModule(String module) {
        return switch (module) {
            case "Dashboard"     -> buildDashboard();
            case "Employees"     -> buildEmployees();
            case "Departments"   -> buildDepartments();
            case "Attendance"    -> buildAttendance();
            case "Inventory"     -> buildInventory();
            case "Categories"    -> buildCategories();
            case "Suppliers"     -> buildSuppliers();
            case "Sales"         -> buildSales();
            case "Purchases"     -> buildPurchases();
            case "Expenses"      -> buildExpenses();
            case "Payroll"       -> buildPayroll();
            case "Reports"       -> buildReports();
            case "Notifications" -> buildNotifications();
            case "Settings"      -> buildSettings();
            default              -> buildDashboard();
        };
    }

    // ============================================================
    // DASHBOARD MODULE
    // ============================================================
    private Node buildDashboard() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        // KPI Cards
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);

        Object[][] kpis = {
            {"Total Employees",   String.valueOf(getTotalEmployees()), "↑ 8.4%",   ACCENT,   "👥"},
            {"Present Today",     String.valueOf(getPresentToday()),   "↑ 5.2%",   SUCCESS,  "✅"},
            {"Total Sales",       fmt(getTotalSales()),                "↑ 12.1%",  SUCCESS,  "💰"},
            {"Total Expenses",    fmt(getTotalExpenses()),             "↑ 3.5%",   DANGER,   "💸"},
            {"Inventory Value",   fmt(getInventoryValue()),            "↓ 1.2%",   PURPLE,   "📦"},
            {"Pending Payments",  String.valueOf(getPendingPayments()),"↓ 2",      WARNING,  "⏳"},
            {"Monthly Payroll",   fmt(getMonthlyPayroll()),            "→ 0.0%",   TEAL,     "💵"},
            {"Low Stock Items",   String.valueOf(getLowStockCount()),  "⚠ Alert",  ORANGE,   "⚠"}
        };

        Label[] valLabels = new Label[8];
        for (int i = 0; i < kpis.length; i++) {
            Object[] k = kpis[i];
            VBox card = createKPICard((String)k[0], (String)k[1], (String)k[2], (String)k[3], (String)k[4]);
            Label valLbl = (Label) ((VBox)((HBox)card.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
            valLabels[i] = valLbl;
            kpiGrid.add(card, i % 4, i / 4);
        }

        kpiEmployees = valLabels[0]; kpiPresent    = valLabels[1];
        kpiSales     = valLabels[2]; kpiExpenses   = valLabels[3];
        kpiInventory = valLabels[4]; kpiPending    = valLabels[5];
        kpiPayroll   = valLabels[6]; kpiLowStock   = valLabels[7];

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            kpiGrid.getColumnConstraints().add(cc);
        }

        // Charts row
        HBox chartsRow = new HBox(16);

        // Sales LineChart
        VBox salesChartCard = createCard("Sales Overview — Last 7 Days", buildSalesLineChart());
        HBox.setHgrow(salesChartCard, Priority.ALWAYS);

        // Pie chart
        VBox pieChartCard = createCard("Sales by Category", buildCategoryPieChart());
        pieChartCard.setPrefWidth(280);

        chartsRow.getChildren().addAll(salesChartCard, pieChartCard);

        // Second charts row
        HBox chartsRow2 = new HBox(16);
        VBox invBarCard  = createCard("Inventory Status", buildInventoryBarChart());
        VBox attBarCard  = createCard("Attendance Overview", buildAttendanceBarChart());
        HBox.setHgrow(invBarCard,  Priority.ALWAYS);
        HBox.setHgrow(attBarCard,  Priority.ALWAYS);
        chartsRow2.getChildren().addAll(invBarCard, attBarCard);

        // Activity feed
        VBox actCard = createCard("Recent Activities", buildActivityFeed());

        page.getChildren().addAll(kpiGrid, chartsRow, chartsRow2, actCard);
        return page;
    }

    private VBox createKPICard(String title, String value, String trend, String color, String icon) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                      "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        card.setPadding(new Insets(20));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(6);
        HBox.setHgrow(left, Priority.ALWAYS);
        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getSub() + "; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");
        left.getChildren().addAll(titleLbl, valueLbl);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 28px; -fx-background-color: " + color + "22; " +
                         "-fx-background-radius: 10; -fx-padding: 8;");

        top.getChildren().addAll(left, iconLbl);

        boolean isPositive = trend.startsWith("↑");
        boolean isNeutral  = trend.startsWith("→");
        String trendColor  = isNeutral ? getSub() : (isPositive ? SUCCESS : DANGER);
        Label trendLbl = new Label(trend);
        trendLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + trendColor + "; -fx-font-weight: bold; -fx-padding: 8 0 0 0;");

        card.getChildren().addAll(top, trendLbl);
        return card;
    }

    private VBox createCard(String title, Node content) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                      "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        card.setPadding(new Insets(20));
        if (title != null && !title.isEmpty()) {
            Label t = new Label(title);
            t.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: " + getBorder() + ";");
            card.getChildren().addAll(t, sep);
        }
        card.getChildren().add(content);
        return card;
    }

    private Node buildSalesLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        yAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        xAxis.setLabel("Day");
        yAxis.setLabel("Sales (Rs.)");
        salesLineChart = new LineChart<>(xAxis, yAxis);
        salesLineChart.setStyle("-fx-background-color: transparent;");
        salesLineChart.setPrefHeight(200);
        salesLineChart.setLegendVisible(false);
        salesLineChart.setAnimated(false);
        refreshSalesLineChart();
        return salesLineChart;
    }

    private void refreshSalesLineChart() {
        if (salesLineChart == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        DateTimeFormatter disp= DateTimeFormatter.ofPattern("dd MMM");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String ds   = d.format(fmt);
            double total= sales.stream().filter(s -> s.getDate().equals(ds)).mapToDouble(Sale::getGrandTotal).sum();
            series.getData().add(new XYChart.Data<>(d.format(disp), total));
        }
        Platform.runLater(() -> {
            salesLineChart.getData().clear();
            salesLineChart.getData().add(series);
        });
    }

    private Node buildCategoryPieChart() {
        categoryPieChart = new PieChart();
        categoryPieChart.setPrefHeight(200);
        categoryPieChart.setAnimated(false);
        categoryPieChart.setLegendSide(Side.BOTTOM);
        refreshCategoryPieChart();
        return categoryPieChart;
    }

    private void refreshCategoryPieChart() {
        if (categoryPieChart == null) return;
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        Map<String, Double> catSales = new HashMap<>();
        for (Sale s : sales) {
            for (SaleItem si : s.items) {
                String cat = products.stream().filter(p -> p.getId().equals(si.productId))
                                     .map(Product::getCategory).findFirst().orElse("Other");
                catSales.merge(cat, si.total, Double::sum);
            }
        }
        catSales.forEach((cat, total) -> data.add(new PieChart.Data(cat, total)));
        Platform.runLater(() -> categoryPieChart.setData(data));
    }

    private Node buildInventoryBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        yAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        inventoryBarChart = new BarChart<>(xAxis, yAxis);
        inventoryBarChart.setStyle("-fx-background-color: transparent;");
        inventoryBarChart.setPrefHeight(200);
        inventoryBarChart.setAnimated(false);
        inventoryBarChart.setCategoryGap(30);
        refreshInventoryBarChart();
        return inventoryBarChart;
    }

    private void refreshInventoryBarChart() {
        if (inventoryBarChart == null) return;
        XYChart.Series<String, Number> s1 = new XYChart.Series<>(); s1.setName("In Stock");
        XYChart.Series<String, Number> s2 = new XYChart.Series<>(); s2.setName("Low Stock");
        XYChart.Series<String, Number> s3 = new XYChart.Series<>(); s3.setName("Out of Stock");
        long inStock  = products.stream().filter(p -> p.getStatus().equals("IN STOCK")).count();
        long lowStock = products.stream().filter(p -> p.getStatus().equals("LOW STOCK")).count();
        long outStock = products.stream().filter(p -> p.getStatus().equals("OUT OF STOCK")).count();
        s1.getData().add(new XYChart.Data<>("Status", inStock));
        s2.getData().add(new XYChart.Data<>("Status", lowStock));
        s3.getData().add(new XYChart.Data<>("Status", outStock));
        Platform.runLater(() -> {
            inventoryBarChart.getData().clear();
            inventoryBarChart.getData().addAll(s1, s2, s3);
        });
    }

    private Node buildAttendanceBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        yAxis.setStyle("-fx-tick-label-fill: " + getSub() + ";");
        attendanceBarChart = new BarChart<>(xAxis, yAxis);
        attendanceBarChart.setStyle("-fx-background-color: transparent;");
        attendanceBarChart.setPrefHeight(200);
        attendanceBarChart.setAnimated(false);
        refreshAttendanceBarChart();
        return attendanceBarChart;
    }

    private void refreshAttendanceBarChart() {
        if (attendanceBarChart == null) return;
        String td = today();
        long present = attendance.stream().filter(a -> a.getDate().equals(td) &&
                (a.getStatus().contains("Present") || a.getStatus().contains("Checked"))).count();
        long absent  = attendance.stream().filter(a -> a.getDate().equals(td) && a.getStatus().equals("Absent")).count();
        long late    = attendance.stream().filter(a -> a.getDate().equals(td) && a.getStatus().equals("Late")).count();
        long leave   = attendance.stream().filter(a -> a.getDate().equals(td) && a.getStatus().equals("Leave")).count();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Today");
        s.getData().add(new XYChart.Data<>("Present", present));
        s.getData().add(new XYChart.Data<>("Absent",  absent));
        s.getData().add(new XYChart.Data<>("Late",    late));
        s.getData().add(new XYChart.Data<>("Leave",   leave));
        Platform.runLater(() -> {
            attendanceBarChart.getData().clear();
            attendanceBarChart.getData().add(s);
        });
    }

    private Node buildActivityFeed() {
        activityFeed = new VBox(0);
        refreshActivityFeed();
        return activityFeed;
    }

    private void refreshActivityFeed() {
        if (activityFeed == null) return;
        Platform.runLater(() -> {
            activityFeed.getChildren().clear();
            int limit = Math.min(activities.size(), 6);
            for (int i = 0; i < limit; i++) {
                Label lbl = new Label(activities.get(i));
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getText() + "; " +
                             "-fx-padding: 8 0 8 0; -fx-border-color: transparent transparent " + getBorder() + " transparent; -fx-border-width: 0 0 1 0;");
                lbl.setWrapText(true);
                activityFeed.getChildren().add(lbl);
            }
        });
    }

    private void refreshAllCharts() {
        refreshSalesLineChart();
        refreshCategoryPieChart();
        refreshInventoryBarChart();
        refreshAttendanceBarChart();
    }

    // ============================================================
    // EMPLOYEES MODULE
    // ============================================================
    private Node buildEmployees() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField search = new TextField();
        search.setPromptText("🔍  Search employees...");
        search.setPrefWidth(280);
        styleTextField(search);
        HBox.setHgrow(search, Priority.ALWAYS);

        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All Departments");
        departments.forEach(d -> deptFilter.getItems().add(d.getName()));
        deptFilter.setValue("All Departments");
        styleComboBox(deptFilter);

        Button addBtn = createPrimaryButton("+ Add Employee");
        toolbar.getChildren().addAll(search, deptFilter, addBtn);

        // Stats
        HBox stats = new HBox(12);
        stats.getChildren().addAll(
            createSmallStat("Total", String.valueOf(employees.size()), ACCENT),
            createSmallStat("Active", String.valueOf(employees.stream().filter(e->e.getStatus().equals("Active")).count()), SUCCESS),
            createSmallStat("On Leave", String.valueOf(employees.stream().filter(e->e.getStatus().equals("On Leave")).count()), WARNING),
            createSmallStat("Inactive", String.valueOf(employees.stream().filter(e->e.getStatus().equals("Inactive")).count()), DANGER)
        );

        // Table
        TableView<Employee> table = new TableView<>();
        styleTable(table);
        table.setPrefHeight(460);

        ObservableList<Employee> filtered = FXCollections.observableArrayList(employees);
        table.setItems(filtered);

        TableColumn<Employee,String> colId   = col("Emp ID",     "id",         100);
        TableColumn<Employee,String> colName = col("Name",       "name",       160);
        TableColumn<Employee,String> colDept = col("Department", "department", 160);
        TableColumn<Employee,String> colPos  = col("Position",   "position",   140);
        TableColumn<Employee,Number> colSal  = numCol("Salary",  "salary",     120);
        TableColumn<Employee,String> colStat = col("Status",     "status",     90);

        colSal.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt(item.doubleValue()));
            }
        });
        colStat.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = item.equals("Active") ? SUCCESS : item.equals("On Leave") ? WARNING : DANGER;
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });

        TableColumn<Employee,Void> colAct = new TableColumn<>("Actions");
        colAct.setPrefWidth(160);
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = createSmallButton("Edit", ACCENT);
            private final Button delBtn  = createSmallButton("Delete", DANGER);
            {
                editBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    showEmployeeDialog(emp, table);
                });
                delBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    if (confirmDelete("employee", emp.getName())) {
                        employees.remove(emp);
                        filtered.setAll(employees);
                        updateDeptCounts();
                        refreshDashboardKPIs();
                        addActivity("Employee '" + emp.getName() + "' removed");
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, editBtn, delBtn);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colName, colDept, colPos, colSal, colStat, colAct);

        // Search filter
        search.textProperty().addListener((obs, ov, nv) -> {
            String q = nv.toLowerCase();
            String dept = deptFilter.getValue();
            filtered.setAll(employees.stream()
                .filter(e -> (e.getName().toLowerCase().contains(q) ||
                              e.getEmail().toLowerCase().contains(q) ||
                              e.getId().toLowerCase().contains(q)) &&
                             (dept.equals("All Departments") || e.getDepartment().equals(dept)))
                .toList());
        });
        deptFilter.setOnAction(e -> {
            String q = search.getText().toLowerCase();
            String dept = deptFilter.getValue();
            filtered.setAll(employees.stream()
                .filter(emp -> (emp.getName().toLowerCase().contains(q) ||
                                emp.getEmail().toLowerCase().contains(q)) &&
                               (dept.equals("All Departments") || emp.getDepartment().equals(dept)))
                .toList());
        });

        addBtn.setOnAction(e -> showEmployeeDialog(null, table));

        VBox tableCard = createCard("", table);
        page.getChildren().addAll(toolbar, stats, tableCard);
        return page;
    }

    private void showEmployeeDialog(Employee emp, TableView<Employee> table) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(emp == null ? "Add New Employee" : "Edit Employee");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(500);
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField fName  = styledField(emp != null ? emp.getName() : "",        "Full Name");
        TextField fFather= styledField(emp != null ? emp.getFatherName() : "",  "Father Name");
        TextField fEmail = styledField(emp != null ? emp.getEmail() : "",       "Email");
        TextField fPhone = styledField(emp != null ? emp.getPhone() : "",       "Phone");
        ComboBox<String> fDept = new ComboBox<>();
        departments.forEach(d -> fDept.getItems().add(d.getName()));
        if (emp != null) fDept.setValue(emp.getDepartment());
        else fDept.setValue(departments.isEmpty() ? "" : departments.get(0).getName());
        styleComboBox(fDept);
        TextField fPos   = styledField(emp != null ? emp.getPosition() : "",    "Position");
        TextField fDate  = styledField(emp != null ? emp.getJoiningDate() : today(), "Joining Date");
        TextField fSal   = styledField(emp != null ? String.valueOf((int)emp.getSalary()) : "", "Basic Salary");
        ComboBox<String> fStat = new ComboBox<>();
        fStat.getItems().addAll("Active","On Leave","Inactive");
        fStat.setValue(emp != null ? emp.getStatus() : "Active");
        styleComboBox(fStat);

        grid.addRow(0, fieldLabel("Full Name"),    fName,  fieldLabel("Father Name"), fFather);
        grid.addRow(1, fieldLabel("Email"),        fEmail, fieldLabel("Phone"),       fPhone);
        grid.addRow(2, fieldLabel("Department"),   fDept,  fieldLabel("Position"),    fPos);
        grid.addRow(3, fieldLabel("Joining Date"), fDate,  fieldLabel("Basic Salary"),fSal);
        grid.addRow(4, fieldLabel("Status"),       fStat);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(emp == null ? "Add Employee" : "Save Changes");
        styleDialogButton(okBtn, ACCENT);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fName.getText().isBlank() || fSal.getText().isBlank()) {
                    showError("Validation Error", "Name and Salary are required.");
                    return null;
                }
                double sal;
                try { sal = Double.parseDouble(fSal.getText()); }
                catch (NumberFormatException ex) { showError("Invalid Input", "Salary must be a valid number."); return null; }
                if (sal < 0) { showError("Invalid Input", "Salary cannot be negative."); return null; }
                if (emp == null) {
                    Employee ne = new Employee(fName.getText(), fFather.getText(), fEmail.getText(),
                                              fPhone.getText(), fDept.getValue(), fPos.getText(),
                                              fDate.getText(), sal, fStat.getValue());
                    employees.add(ne);
                    updateDeptCounts();
                    refreshDashboardKPIs();
                    addActivity("New employee '" + ne.getName() + "' added");
                    addNotification("success", "Employee Added", ne.getName() + " has been added.");
                } else {
                    emp.setName(fName.getText()); emp.setFatherName(fFather.getText());
                    emp.setEmail(fEmail.getText()); emp.setPhone(fPhone.getText());
                    emp.setDepartment(fDept.getValue()); emp.setPosition(fPos.getText());
                    emp.setJoiningDate(fDate.getText()); emp.setSalary(sal);
                    emp.setStatus(fStat.getValue());
                    updateDeptCounts();
                    refreshDashboardKPIs();
                    addActivity("Employee '" + emp.getName() + "' updated");
                }
                table.refresh();
                return emp;
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // DEPARTMENTS MODULE
    // ============================================================
    private Node buildDepartments() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        Button addBtn = createPrimaryButton("+ Add Department");
        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        TableView<Department> table = new TableView<>(departments);
        styleTable(table);
        table.setPrefHeight(500);

        TableColumn<Department,String> colId   = col("Dept ID",    "id",            100);
        TableColumn<Department,String> colName = col("Department", "name",          180);
        TableColumn<Department,String> colMgr  = col("Manager",    "manager",       160);
        TableColumn<Department,Number> colEmp  = numCol("Employees","employeeCount", 100);
        TableColumn<Department,String> colDesc = col("Description","description",   240);
        TableColumn<Department,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(140);
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = createSmallButton("Edit", ACCENT);
            private final Button delBtn  = createSmallButton("Delete", DANGER);
            {
                editBtn.setOnAction(e -> {
                    Department d = getTableView().getItems().get(getIndex());
                    showDeptDialog(d, table);
                });
                delBtn.setOnAction(e -> {
                    Department d = getTableView().getItems().get(getIndex());
                    if (d.getEmployeeCount() > 0) { showError("Cannot Delete","Department has employees assigned."); return; }
                    if (confirmDelete("department", d.getName())) {
                        departments.remove(d);
                        table.refresh();
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(6, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colId, colName, colMgr, colEmp, colDesc, colAct);
        addBtn.setOnAction(e -> showDeptDialog(null, table));

        page.getChildren().addAll(toolbar, createCard("", table));
        return page;
    }

    private void showDeptDialog(Department dept, TableView<Department> table) {
        Dialog<Department> dialog = new Dialog<>();
        dialog.setTitle(dept == null ? "Add Department" : "Edit Department");
        dialog.getDialogPane().setPrefWidth(420);
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField fName = styledField(dept != null ? dept.getName()    : "", "Department Name");
        TextField fMgr  = styledField(dept != null ? dept.getManager() : "", "Manager Name");
        TextField fDesc = styledField(dept != null ? dept.getDescription() : "", "Description");

        grid.addRow(0, fieldLabel("Name"),        fName);
        grid.addRow(1, fieldLabel("Manager"),     fMgr);
        grid.addRow(2, fieldLabel("Description"), fDesc);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(dept == null ? "Add" : "Save");
        styleDialogButton(okBtn, ACCENT);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fName.getText().isBlank()) { showError("Error", "Department name is required."); return null; }
                if (dept == null) {
                    departments.add(new Department(fName.getText(), fMgr.getText(), fDesc.getText()));
                } else {
                    dept.setName(fName.getText()); dept.setManager(fMgr.getText()); dept.setDescription(fDesc.getText());
                }
                table.refresh();
                return dept;
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // ATTENDANCE MODULE
    // ============================================================
    private Node buildAttendance() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Clock card
        VBox clockCard = new VBox(8);
        clockCard.setAlignment(Pos.CENTER);
        clockCard.setStyle("-fx-background-color: #1A2236; -fx-background-radius: 12; -fx-padding: 24;");
        clockLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        clockLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;");
        dateLabel  = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.6);");
        clockCard.getChildren().addAll(clockLabel, dateLabel);

        // Check In/Out panel
        HBox checkPanel = new HBox(16);
        checkPanel.setAlignment(Pos.CENTER);
        ComboBox<String> empSelect = new ComboBox<>();
        empSelect.getItems().add("Select Employee...");
        employees.forEach(e -> empSelect.getItems().add(e.getId() + " - " + e.getName()));
        empSelect.setValue("Select Employee...");
        empSelect.setPrefWidth(280);
        styleComboBox(empSelect);

        ComboBox<String> statusSelect = new ComboBox<>();
        statusSelect.getItems().addAll("Present","Late","Leave");
        statusSelect.setValue("Present");
        styleComboBox(statusSelect);

        Button checkInBtn  = createPrimaryButton("✔ Check In");
        Button checkOutBtn = createButton("✖ Check Out", WARNING);

        checkInBtn.setOnAction(e -> {
            if (empSelect.getValue().equals("Select Employee...")) { showError("Error","Please select an employee."); return; }
            String[] parts = empSelect.getValue().split(" - ", 2);
            String empId = parts[0], empName = parts[1];
            String td = today();
            boolean exists = attendance.stream().anyMatch(a -> a.getEmployeeId().equals(empId) && a.getDate().equals(td));
            if (exists) { showError("Already Checked In", empName + " has already checked in today."); return; }
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
            attendance.add(0, new AttendanceRecord(empId, empName, td, time, "-", statusSelect.getValue()));
            refreshDashboardKPIs();
            refreshAttendanceBarChart();
            addActivity(empName + " checked in at " + time);
            addNotification("success","Attendance","" + empName + " checked in at " + time);
            showSuccess("Check In", empName + " checked in successfully at " + time);
        });

        checkOutBtn.setOnAction(e -> {
            if (empSelect.getValue().equals("Select Employee...")) { showError("Error","Please select an employee."); return; }
            String[] parts = empSelect.getValue().split(" - ", 2);
            String empId = parts[0], empName = parts[1];
            String td = today();
            Optional<AttendanceRecord> rec = attendance.stream()
                .filter(a -> a.getEmployeeId().equals(empId) && a.getDate().equals(td)).findFirst();
            if (rec.isEmpty()) { showError("Not Checked In", empName + " has not checked in today."); return; }
            if (!rec.get().getCheckOut().equals("-")) { showError("Already Checked Out", empName + " has already checked out."); return; }
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
            rec.get().setCheckOut(time);
            rec.get().setStatus("Checked Out");
            addActivity(empName + " checked out at " + time);
            showSuccess("Check Out", empName + " checked out at " + time);
        });

        checkPanel.getChildren().addAll(empSelect, statusSelect, checkInBtn, checkOutBtn);

        // Today's Stats
        HBox todayStats = new HBox(12);
        String td = today();
        long present = attendance.stream().filter(a -> a.getDate().equals(td) &&
                (a.getStatus().equals("Present") || a.getStatus().equals("Checked In") || a.getStatus().equals("Checked Out") || a.getStatus().equals("Late"))).count();
        long absent  = attendance.stream().filter(a -> a.getDate().equals(td) && a.getStatus().equals("Absent")).count();
        long leave   = attendance.stream().filter(a -> a.getDate().equals(td) && a.getStatus().equals("Leave")).count();
        todayStats.getChildren().addAll(
            createSmallStat("Present Today", String.valueOf(present), SUCCESS),
            createSmallStat("Absent Today",  String.valueOf(absent),  DANGER),
            createSmallStat("On Leave",      String.valueOf(leave),   WARNING),
            createSmallStat("Total Employees", String.valueOf(employees.size()), ACCENT)
        );

        // Attendance table
        TableView<AttendanceRecord> table = new TableView<>(attendance);
        styleTable(table);
        table.setPrefHeight(380);
        table.getColumns().addAll(
            col("Att ID",    "id",           90),
            col("Emp ID",    "employeeId",  100),
            col("Name",      "employeeName",160),
            col("Date",      "date",        120),
            col("Check In",  "checkIn",     100),
            col("Check Out", "checkOut",    100),
            col("Status",    "status",      100)
        );

        page.getChildren().addAll(clockCard, checkPanel, createCard("Today's Summary", todayStats),
                                  createCard("Attendance Records", table));
        return page;
    }

    // ============================================================
    // INVENTORY MODULE
    // ============================================================
    private Node buildInventory() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Toolbar
        TextField search = new TextField();
        search.setPromptText("🔍  Search products...");
        search.setPrefWidth(260);
        styleTextField(search);
        ComboBox<String> catFilter = new ComboBox<>();
        catFilter.getItems().add("All Categories");
        categories.forEach(c -> catFilter.getItems().add(c.getName()));
        catFilter.setValue("All Categories");
        styleComboBox(catFilter);
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status","IN STOCK","LOW STOCK","OUT OF STOCK");
        statusFilter.setValue("All Status");
        styleComboBox(statusFilter);
        Button addBtn = createPrimaryButton("+ Add Product");
        HBox toolbar = new HBox(12, search, catFilter, statusFilter, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        // Stats
        HBox stats = new HBox(12);
        stats.getChildren().addAll(
            createSmallStat("Total Products",  String.valueOf(products.size()), ACCENT),
            createSmallStat("In Stock",        String.valueOf(products.stream().filter(p->p.getStatus().equals("IN STOCK")).count()), SUCCESS),
            createSmallStat("Low Stock",       String.valueOf(products.stream().filter(p->p.getStatus().equals("LOW STOCK")).count()), WARNING),
            createSmallStat("Out of Stock",    String.valueOf(products.stream().filter(p->p.getStatus().equals("OUT OF STOCK")).count()), DANGER),
            createSmallStat("Inventory Value", fmt(getInventoryValue()), PURPLE)
        );

        // Table
        ObservableList<Product> filtered = FXCollections.observableArrayList(products);
        TableView<Product> table = new TableView<>(filtered);
        styleTable(table);
        table.setPrefHeight(440);

        TableColumn<Product,String> colId   = col("Prod ID",     "id",           100);
        TableColumn<Product,String> colName = col("Product Name","name",         200);
        TableColumn<Product,String> colCat  = col("Category",    "category",     130);
        TableColumn<Product,String> colSku  = col("SKU",         "sku",           90);
        TableColumn<Product,Number> colQty  = numCol("Quantity", "quantity",      80);
        TableColumn<Product,Number> colSP   = numCol("Sale Price","sellingPrice", 110);
        TableColumn<Product,String> colStat = col("Status",      "status",        110);
        TableColumn<Product,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(220);

        colSP.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt(item.doubleValue()));
            }
        });
        colStat.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = item.equals("IN STOCK") ? SUCCESS : item.equals("LOW STOCK") ? WARNING : DANGER;
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn   = createSmallButton("Edit",     ACCENT);
            private final Button addQBtn   = createSmallButton("+Stock",   SUCCESS);
            private final Button delBtn    = createSmallButton("Delete",   DANGER);
            {
                editBtn.setOnAction(e -> { Product p = getTableView().getItems().get(getIndex()); showProductDialog(p, table, filtered); });
                addQBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    TextInputDialog tid = new TextInputDialog("10");
                    tid.setTitle("Add Stock"); tid.setHeaderText(null);
                    tid.setContentText("Add quantity for " + p.getName() + ":");
                    styleDialog(tid);
                    tid.showAndWait().ifPresent(v -> {
                        try {
                            int add = Integer.parseInt(v);
                            if (add <= 0) { showError("Error","Quantity must be positive."); return; }
                            p.setQuantity(p.getQuantity() + add);
                            filtered.setAll(applyInvFilter(search.getText(), catFilter.getValue(), statusFilter.getValue()));
                            refreshInventoryBarChart();
                            refreshDashboardKPIs();
                            addActivity("Stock for '" + p.getName() + "' increased by " + add);
                        } catch (NumberFormatException ex) { showError("Error","Enter a valid number."); }
                    });
                });
                delBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    if (confirmDelete("product", p.getName())) {
                        products.remove(p);
                        filtered.setAll(applyInvFilter(search.getText(), catFilter.getValue(), statusFilter.getValue()));
                        updateCatCounts();
                        refreshDashboardKPIs();
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(4, editBtn, addQBtn, delBtn));
            }
        });

        table.getColumns().addAll(colId, colName, colCat, colSku, colQty, colSP, colStat, colAct);

        Runnable applyFilter = () -> filtered.setAll(applyInvFilter(search.getText(), catFilter.getValue(), statusFilter.getValue()));
        search.textProperty().addListener((o,ov,nv) -> applyFilter.run());
        catFilter.setOnAction(e -> applyFilter.run());
        statusFilter.setOnAction(e -> applyFilter.run());
        addBtn.setOnAction(e -> showProductDialog(null, table, filtered));

        page.getChildren().addAll(toolbar, createCard("Inventory Overview", stats), createCard("", table));
        return page;
    }

    private List<Product> applyInvFilter(String q, String cat, String status) {
        q = q.toLowerCase();
        final String fq = q, fcat = cat, fst = status;
        return products.stream().filter(p ->
            (p.getName().toLowerCase().contains(fq) || p.getSku().toLowerCase().contains(fq)) &&
            (fcat.equals("All Categories") || p.getCategory().equals(fcat)) &&
            (fst.equals("All Status")      || p.getStatus().equals(fst))
        ).toList();
    }

    private void showProductDialog(Product prod, TableView<Product> table, ObservableList<Product> filtered) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(prod == null ? "Add Product" : "Edit Product");
        dialog.getDialogPane().setPrefWidth(520);
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField fName = styledField(prod != null ? prod.getName()    : "", "Product Name");
        TextField fSku  = styledField(prod != null ? prod.getSku()     : "", "SKU");
        ComboBox<String> fCat = new ComboBox<>();
        categories.forEach(c -> fCat.getItems().add(c.getName()));
        fCat.setValue(prod != null ? prod.getCategory() : (categories.isEmpty() ? "" : categories.get(0).getName()));
        styleComboBox(fCat);
        ComboBox<String> fSup = new ComboBox<>();
        suppliers.forEach(s -> fSup.getItems().add(s.getName()));
        fSup.setValue(prod != null ? prod.getSupplier() : (suppliers.isEmpty() ? "" : suppliers.get(0).getName()));
        styleComboBox(fSup);
        TextField fPP  = styledField(prod != null ? String.valueOf((int)prod.getPurchasePrice()) : "", "Purchase Price");
        TextField fSP  = styledField(prod != null ? String.valueOf((int)prod.getSellingPrice())  : "", "Selling Price");
        TextField fQty = styledField(prod != null ? String.valueOf(prod.getQuantity())           : "", "Quantity");
        TextField fMin = styledField(prod != null ? String.valueOf(prod.getMinStock())           : "", "Min Stock");

        grid.addRow(0, fieldLabel("Product Name"), fName, fieldLabel("SKU"),            fSku);
        grid.addRow(1, fieldLabel("Category"),     fCat,  fieldLabel("Supplier"),       fSup);
        grid.addRow(2, fieldLabel("Purchase Price"),fPP,  fieldLabel("Selling Price"),  fSP);
        grid.addRow(3, fieldLabel("Quantity"),     fQty,  fieldLabel("Min Stock"),      fMin);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(prod == null ? "Add Product" : "Save Changes");
        styleDialogButton(okBtn, ACCENT);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fName.getText().isBlank()) { showError("Error","Product name is required."); return null; }
                try {
                    double pp  = Double.parseDouble(fPP.getText());
                    double sp  = Double.parseDouble(fSP.getText());
                    int qty    = Integer.parseInt(fQty.getText());
                    int min    = Integer.parseInt(fMin.getText());
                    if (pp < 0 || sp < 0 || qty < 0 || min < 0) { showError("Error","Values cannot be negative."); return null; }
                    if (prod == null) {
                        Product np = new Product(fName.getText(), fCat.getValue(), fSku.getText(),
                                                 fSup.getValue(), pp, sp, qty, min);
                        products.add(np);
                        addActivity("New product '" + np.getName() + "' added");
                    } else {
                        prod.setName(fName.getText()); prod.setCategory(fCat.getValue());
                        prod.setSku(fSku.getText()); prod.setSupplier(fSup.getValue());
                        prod.setPurchasePrice(pp); prod.setSellingPrice(sp);
                        prod.setQuantity(qty); prod.setMinStock(min);
                    }
                    filtered.setAll(products);
                    updateCatCounts(); refreshDashboardKPIs();
                    if (prod == null && getLowStockCount() > 0) {
                        long ls = products.stream().filter(p -> p.getStatus().equals("LOW STOCK") || p.getStatus().equals("OUT OF STOCK")).count();
                        addNotification("warning","Low Stock Alert", ls + " products have low or zero stock.");
                    }
                    table.refresh();
                } catch (NumberFormatException ex) { showError("Error","Please enter valid numbers."); return null; }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // CATEGORIES MODULE
    // ============================================================
    private Node buildCategories() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        Button addBtn = createPrimaryButton("+ Add Category");
        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        TableView<Category> table = new TableView<>(categories);
        styleTable(table);
        table.setPrefHeight(500);

        TableColumn<Category,String> colId   = col("Cat ID",      "id",           80);
        TableColumn<Category,String> colName = col("Category",    "name",        180);
        TableColumn<Category,String> colDesc = col("Description", "description", 300);
        TableColumn<Category,Number> colProd = numCol("Products", "productCount", 90);
        TableColumn<Category,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(140);
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = createSmallButton("Edit",   ACCENT);
            private final Button delBtn  = createSmallButton("Delete", DANGER);
            {
                editBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    showCategoryDialog(cat, table);
                });
                delBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    if (cat.getProductCount() > 0) { showError("Cannot Delete","Category has products assigned."); return; }
                    if (confirmDelete("category", cat.getName())) { categories.remove(cat); table.refresh(); }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(6, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colId, colName, colDesc, colProd, colAct);
        addBtn.setOnAction(e -> showCategoryDialog(null, table));
        page.getChildren().addAll(toolbar, createCard("", table));
        return page;
    }

    private void showCategoryDialog(Category cat, TableView<Category> table) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(cat == null ? "Add Category" : "Edit Category");
        dialog.getDialogPane().setPrefWidth(380);
        styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField fName = styledField(cat != null ? cat.getName()        : "", "Category Name");
        TextField fDesc = styledField(cat != null ? cat.getDescription() : "", "Description");
        grid.addRow(0, fieldLabel("Name"),        fName);
        grid.addRow(1, fieldLabel("Description"), fDesc);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(cat == null ? "Add" : "Save");
        styleDialogButton(okBtn, ACCENT);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fName.getText().isBlank()) { showError("Error","Category name is required."); return null; }
                if (cat == null) categories.add(new Category(fName.getText(), fDesc.getText()));
                else { cat.setName(fName.getText()); cat.setDescription(fDesc.getText()); }
                table.refresh();
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // SUPPLIERS MODULE
    // ============================================================
    private Node buildSuppliers() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        TextField search = new TextField();
        search.setPromptText("🔍  Search suppliers...");
        search.setPrefWidth(280);
        styleTextField(search);
        Button addBtn = createPrimaryButton("+ Add Supplier");
        HBox toolbar = new HBox(12, search, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        ObservableList<Supplier> filtered = FXCollections.observableArrayList(suppliers);
        TableView<Supplier> table = new TableView<>(filtered);
        styleTable(table);
        table.setPrefHeight(500);

        TableColumn<Supplier,String> colId   = col("Sup ID",   "id",            80);
        TableColumn<Supplier,String> colName = col("Name",     "name",          150);
        TableColumn<Supplier,String> colComp = col("Company",  "company",       180);
        TableColumn<Supplier,String> colPh   = col("Phone",    "phone",         120);
        TableColumn<Supplier,String> colEm   = col("Email",    "email",         180);
        TableColumn<Supplier,String> colPay  = col("Payment",  "paymentStatus",  90);
        TableColumn<Supplier,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(140);

        colPay.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = item.equals("Paid") ? SUCCESS : WARNING;
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = createSmallButton("Edit",   ACCENT);
            private final Button delBtn  = createSmallButton("Delete", DANGER);
            {
                editBtn.setOnAction(e -> { Supplier s = getTableView().getItems().get(getIndex()); showSupplierDialog(s, table, filtered); });
                delBtn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    if (confirmDelete("supplier", s.getName())) {
                        suppliers.remove(s);
                        filtered.setAll(suppliers);
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(6, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colId, colName, colComp, colPh, colEm, colPay, colAct);
        search.textProperty().addListener((o,ov,nv) -> filtered.setAll(suppliers.stream().filter(s ->
            s.getName().toLowerCase().contains(nv.toLowerCase()) ||
            s.getCompany().toLowerCase().contains(nv.toLowerCase())).toList()));
        addBtn.setOnAction(e -> showSupplierDialog(null, table, filtered));
        page.getChildren().addAll(toolbar, createCard("", table));
        return page;
    }

    private void showSupplierDialog(Supplier sup, TableView<Supplier> table, ObservableList<Supplier> filtered) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(sup == null ? "Add Supplier" : "Edit Supplier");
        dialog.getDialogPane().setPrefWidth(480);
        styleDialog(dialog);
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField fName = styledField(sup != null ? sup.getName()    : "", "Contact Name");
        TextField fComp = styledField(sup != null ? sup.getCompany() : "", "Company Name");
        TextField fPh   = styledField(sup != null ? sup.getPhone()   : "", "Phone");
        TextField fEm   = styledField(sup != null ? sup.getEmail()   : "", "Email");
        TextField fAddr = styledField(sup != null ? sup.getAddress() : "", "Address");
        ComboBox<String> fPay = new ComboBox<>();
        fPay.getItems().addAll("Paid","Pending","Overdue");
        fPay.setValue(sup != null ? sup.getPaymentStatus() : "Paid");
        styleComboBox(fPay);
        grid.addRow(0, fieldLabel("Contact Name"),    fName, fieldLabel("Company"),  fComp);
        grid.addRow(1, fieldLabel("Phone"),           fPh,   fieldLabel("Email"),    fEm);
        grid.addRow(2, fieldLabel("Address"),         fAddr, fieldLabel("Payment Status"), fPay);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(sup == null ? "Add Supplier" : "Save");
        styleDialogButton(okBtn, ACCENT);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fName.getText().isBlank() || fComp.getText().isBlank()) { showError("Error","Name and Company are required."); return null; }
                if (sup == null) {
                    suppliers.add(new Supplier(fName.getText(), fComp.getText(), fPh.getText(), fEm.getText(), fAddr.getText(), fPay.getValue()));
                    filtered.setAll(suppliers);
                    addActivity("New supplier '" + fComp.getText() + "' added");
                } else {
                    sup.setName(fName.getText()); sup.setCompany(fComp.getText());
                    sup.setPhone(fPh.getText()); sup.setEmail(fEm.getText());
                    sup.setAddress(fAddr.getText()); sup.setPaymentStatus(fPay.getValue());
                    filtered.setAll(suppliers);
                }
                table.refresh();
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // SALES MODULE (POS)
    // ============================================================
    private Node buildSales() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Sales History header
        HBox tabBar = new HBox(0);
        Button posBtn  = createToggleTab("New Invoice", true);
        Button histBtn = createToggleTab("Sales History", false);
        tabBar.getChildren().addAll(posBtn, histBtn);

        StackPane contentPane = new StackPane();
        Node posPane  = buildPOSPane(contentPane);
        Node histPane = buildSalesHistory();
        contentPane.getChildren().add(posPane);

        posBtn.setOnAction(e -> { contentPane.getChildren().setAll(posPane); setTabActive(posBtn, histBtn); });
        histBtn.setOnAction(e -> { contentPane.getChildren().setAll(histPane); setTabActive(histBtn, posBtn); });

        page.getChildren().addAll(tabBar, contentPane);
        return page;
    }

    private void setTabActive(Button active, Button... others) {
        active.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 6 6 0 0; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        for (Button b : others)
            b.setStyle("-fx-background-color: " + getCard() + "; -fx-text-fill: " + getSub() + "; " +
                       "-fx-background-radius: 6 6 0 0; -fx-padding: 10 20; -fx-cursor: hand; " +
                       "-fx-border-color: " + getBorder() + "; -fx-border-width: 1 1 0 1;");
    }

    private Button createToggleTab(String label, boolean active) {
        Button btn = new Button(label);
        if (active) btn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                                 "-fx-background-radius: 6 6 0 0; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        else        btn.setStyle("-fx-background-color: " + getCard() + "; -fx-text-fill: " + getSub() + "; " +
                                 "-fx-background-radius: 6 6 0 0; -fx-padding: 10 20; -fx-cursor: hand; " +
                                 "-fx-border-color: " + getBorder() + "; -fx-border-width: 1 1 0 1;");
        return btn;
    }

    private Node buildPOSPane(StackPane parent) {
        cart.clear();
        HBox pos = new HBox(16);

        // Left: Product Selection
        VBox leftPanel = new VBox(12);
        leftPanel.setPrefWidth(400);

        TextField custField = styledField("", "Customer Name");
        ComboBox<String> prodSelect = new ComboBox<>();
        products.forEach(p -> prodSelect.getItems().add(p.getId() + " | " + p.getName() + " | " + fmt(p.getSellingPrice())));
        prodSelect.setPromptText("Select Product...");
        prodSelect.setPrefWidth(Double.MAX_VALUE);
        styleComboBox(prodSelect);

        TextField qtyField = styledField("1", "Quantity");
        Button addToCartBtn = createPrimaryButton("Add to Cart →");

        addToCartBtn.setOnAction(e -> {
            if (prodSelect.getValue() == null) { showError("Error","Please select a product."); return; }
            String pid = prodSelect.getValue().split("\\|")[0].trim();
            Optional<Product> opt = products.stream().filter(p -> p.getId().equals(pid)).findFirst();
            if (opt.isEmpty()) return;
            Product p = opt.get();
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); }
            catch (NumberFormatException ex) { showError("Error","Enter a valid quantity."); return; }
            if (qty <= 0) { showError("Error","Quantity must be greater than 0."); return; }
            if (qty > p.getQuantity()) { showError("Insufficient Stock","Only " + p.getQuantity() + " units available."); return; }
            Optional<SaleItem> existing = cart.stream().filter(ci -> ci.productId.equals(pid)).findFirst();
            if (existing.isPresent()) {
                existing.get().quantity += qty;
                existing.get().total   = existing.get().quantity * existing.get().unitPrice;
            } else {
                cart.add(new SaleItem(pid, p.getName(), qty, p.getSellingPrice()));
            }
            prodSelect.setValue(null); qtyField.setText("1");
            refreshCartTable(parent);
        });

        leftPanel.getChildren().addAll(
            new Label("Customer") {{ setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + "; -fx-font-weight: bold;"); }},
            custField,
            new Label("Product") {{ setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + "; -fx-font-weight: bold;"); }},
            prodSelect,
            new Label("Quantity") {{ setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + "; -fx-font-weight: bold;"); }},
            qtyField,
            addToCartBtn
        );
        leftPanel.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                           "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 20;");

        // Right: Cart + Invoice
        VBox rightPanel = new VBox(12);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                            "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 20;");

        Label cartTitle = new Label("Invoice Items");
        cartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");

        TableView<SaleItem> cartTable = new TableView<>(cart);
        styleTable(cartTable);
        cartTable.setPrefHeight(260);

        TableColumn<SaleItem,String> cProd = new TableColumn<>("Product");
        cProd.setPrefWidth(180);
        cProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().productName));
        TableColumn<SaleItem,String> cQty = new TableColumn<>("Qty");
        cQty.setPrefWidth(60);
        cQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().quantity)));
        TableColumn<SaleItem,String> cPrice = new TableColumn<>("Unit Price");
        cPrice.setPrefWidth(100);
        cPrice.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().unitPrice)));
        TableColumn<SaleItem,String> cTotal = new TableColumn<>("Total");
        cTotal.setPrefWidth(110);
        cTotal.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().total)));
        TableColumn<SaleItem,Void> cRem = new TableColumn<>("");
        cRem.setPrefWidth(60);
        cRem.setCellFactory(c -> new TableCell<>() {
            private final Button rm = createSmallButton("✕", DANGER);
            { rm.setOnAction(e -> { cart.remove(getTableView().getItems().get(getIndex())); refreshCartTable(parent); }); }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : rm);
            }
        });
        cartTable.getColumns().addAll(cProd, cQty, cPrice, cTotal, cRem);

        // Totals section
        GridPane totals = new GridPane();
        totals.setHgap(12); totals.setVgap(8);
        totals.setStyle("-fx-background-color: " + getBg() + "; -fx-padding: 12; -fx-background-radius: 8;");

        Label discLbl = new Label("Discount (Rs.):");
        discLbl.setStyle("-fx-text-fill: " + getText() + "; -fx-font-size: 12px;");
        TextField discField = styledField("0", "Discount");
        discField.setPrefWidth(120);

        Label taxLbl = new Label("Tax (%):");
        taxLbl.setStyle("-fx-text-fill: " + getText() + "; -fx-font-size: 12px;");
        TextField taxField = styledField("5", "Tax %");
        taxField.setPrefWidth(80);

        Label subtotalLbl = new Label("Subtotal:"); subtotalLbl.setStyle("-fx-text-fill: " + getText() + ";");
        Label subtotalVal = new Label(fmt(0)); subtotalVal.setStyle("-fx-text-fill: " + getText() + "; -fx-font-weight: bold;");
        Label discValLbl  = new Label("Discount:"); discValLbl.setStyle("-fx-text-fill: " + DANGER + ";");
        Label discVal     = new Label(fmt(0)); discVal.setStyle("-fx-text-fill: " + DANGER + "; -fx-font-weight: bold;");
        Label taxValLbl   = new Label("Tax:"); taxValLbl.setStyle("-fx-text-fill: " + WARNING + ";");
        Label taxVal      = new Label(fmt(0)); taxVal.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-weight: bold;");
        Label grandLbl    = new Label("GRAND TOTAL:"); grandLbl.setStyle("-fx-text-fill: " + getText() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label grandVal    = new Label(fmt(0)); grandVal.setStyle("-fx-text-fill: " + SUCCESS + "; -fx-font-weight: bold; -fx-font-size: 16px;");

        Runnable recalc = () -> {
            double sub  = cart.stream().mapToDouble(ci -> ci.total).sum();
            double disc = 0; try { disc = Double.parseDouble(discField.getText()); } catch (Exception ignore) {}
            double taxP = 5;  try { taxP = Double.parseDouble(taxField.getText()); } catch (Exception ignore) {}
            double tax  = (sub - disc) * taxP / 100;
            double grand= sub - disc + tax;
            subtotalVal.setText(fmt(sub));
            discVal.setText(fmt(disc));
            taxVal.setText(fmt(tax));
            grandVal.setText(fmt(grand));
        };
        discField.textProperty().addListener((o,ov,nv) -> recalc.run());
        taxField.textProperty().addListener((o,ov,nv)  -> recalc.run());
        cart.addListener((ListChangeListener<SaleItem>) c2 -> recalc.run());

        totals.addRow(0, discLbl, discField, taxLbl, taxField);
        Separator totSep = new Separator(); GridPane.setColumnSpan(totSep, 4);
        totals.addRow(1, totSep);
        totals.addRow(2, subtotalLbl, subtotalVal, taxValLbl, taxVal);
        totals.addRow(3, discValLbl,  discVal,     grandLbl,  grandVal);

        Button createInvoiceBtn = createButton("💳  Create Invoice", SUCCESS);
        createInvoiceBtn.setMaxWidth(Double.MAX_VALUE);
        createInvoiceBtn.setStyle(createInvoiceBtn.getStyle() + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12;");

        createInvoiceBtn.setOnAction(e -> {
            if (custField.getText().isBlank()) { showError("Error","Customer name is required."); return; }
            if (cart.isEmpty()) { showError("Error","Cart is empty. Add products first."); return; }
            double disc = 0; try { disc = Double.parseDouble(discField.getText()); } catch (Exception ignore) {}
            double taxP = 5;  try { taxP = Double.parseDouble(taxField.getText()); } catch (Exception ignore) {}
            if (disc < 0) { showError("Error","Discount cannot be negative."); return; }
            // Deduct inventory
            for (SaleItem ci : cart) {
                products.stream().filter(p -> p.getId().equals(ci.productId)).findFirst()
                        .ifPresent(p -> p.setQuantity(p.getQuantity() - ci.quantity));
            }
            Sale newSale = new Sale(custField.getText(), new ArrayList<>(cart), disc, taxP, today());
            sales.add(newSale);
            addActivity("Invoice " + newSale.getId() + " created for " + custField.getText() + " — " + fmt(newSale.getGrandTotal()));
            addNotification("success","Invoice Created",newSale.getId() + " (" + fmt(newSale.getGrandTotal()) + ") completed.");
            refreshDashboardKPIs();
            refreshAllCharts();
            cart.clear();
            custField.setText("");
            discField.setText("0");
            taxField.setText("5");
            showSuccess("Invoice Created","Invoice " + newSale.getId() + " created successfully!\nTotal: " + fmt(newSale.getGrandTotal()));
        });

        rightPanel.getChildren().addAll(cartTitle, cartTable, totals, createInvoiceBtn);
        pos.getChildren().addAll(leftPanel, rightPanel);
        return pos;
    }

    private void refreshCartTable(StackPane parent) {
        // Cart is observable, tables auto-refresh
    }

    private Node buildSalesHistory() {
        VBox pane = new VBox(16);
        pane.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                      "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 20;");

        TextField search = new TextField();
        search.setPromptText("🔍  Search invoices...");
        search.setPrefWidth(280);
        styleTextField(search);
        HBox toolbar = new HBox(12, search);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        ObservableList<Sale> filtered = FXCollections.observableArrayList(sales);
        TableView<Sale> table = new TableView<>(filtered);
        styleTable(table);
        table.setPrefHeight(500);

        TableColumn<Sale,String> colId   = col("Invoice",   "id",          110);
        TableColumn<Sale,String> colCust = col("Customer",  "customer",    180);
        TableColumn<Sale,String> colDate = col("Date",      "date",        120);
        TableColumn<Sale,Number> colGT   = numCol("Total",  "grandTotal",  130);
        TableColumn<Sale,String> colStat = col("Status",    "status",       90);

        colGT.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt(item.doubleValue()));
            }
        });
        colStat.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = item.equals("Paid") ? SUCCESS : WARNING;
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });

        table.getColumns().addAll(colId, colCust, colDate, colGT, colStat);
        search.textProperty().addListener((o,ov,nv) -> filtered.setAll(sales.stream().filter(s ->
            s.getId().toLowerCase().contains(nv.toLowerCase()) ||
            s.getCustomer().toLowerCase().contains(nv.toLowerCase())).toList()));
        sales.addListener((ListChangeListener<Sale>) c -> filtered.setAll(sales));
        pane.getChildren().addAll(toolbar, table);
        return pane;
    }

    // ============================================================
    // PURCHASES MODULE
    // ============================================================
    private Node buildPurchases() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        Button addBtn = createPrimaryButton("+ New Purchase Order");
        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        ObservableList<PurchaseOrder> filtered = FXCollections.observableArrayList(purchases);
        TableView<PurchaseOrder> table = new TableView<>(filtered);
        styleTable(table);
        table.setPrefHeight(500);

        TableColumn<PurchaseOrder,String> colId   = col("PO Number",  "id",        110);
        TableColumn<PurchaseOrder,String> colSup  = col("Supplier",   "supplier",  160);
        TableColumn<PurchaseOrder,String> colProd = col("Product",    "product",   180);
        TableColumn<PurchaseOrder,Number> colQty  = numCol("Qty",     "quantity",   70);
        TableColumn<PurchaseOrder,Number> colTot  = numCol("Total",   "total",     120);
        TableColumn<PurchaseOrder,String> colDate = col("Date",       "date",      120);
        TableColumn<PurchaseOrder,String> colStat = col("Status",     "status",    100);
        TableColumn<PurchaseOrder,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(200);

        colTot.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt(item.doubleValue()));
            }
        });
        colStat.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = switch(item) {
                    case "Received" -> SUCCESS; case "Approved" -> ACCENT;
                    case "Cancelled" -> DANGER; default -> WARNING;
                };
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button rcvBtn = createSmallButton("Receive",  SUCCESS);
            private final Button appBtn = createSmallButton("Approve",  ACCENT);
            private final Button canBtn = createSmallButton("Cancel",   DANGER);
            {
                rcvBtn.setOnAction(e -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    if (po.getStatus().equals("Received")) { showError("Already Received","This PO is already received."); return; }
                    if (po.getStatus().equals("Cancelled")) { showError("Cancelled","This PO was cancelled."); return; }
                    po.setStatus("Received");
                    products.stream().filter(p -> p.getId().equals(po.productId)).findFirst()
                            .ifPresent(p -> p.setQuantity(p.getQuantity() + po.getQuantity()));
                    table.refresh();
                    refreshDashboardKPIs(); refreshInventoryBarChart();
                    addActivity("Purchase Order " + po.getId() + " received — " + po.getQuantity() + " units of " + po.getProduct());
                    addNotification("success","PO Received", po.getId() + " received. Stock updated for " + po.getProduct() + ".");
                });
                appBtn.setOnAction(e -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    if (!po.getStatus().equals("Pending")) { showError("Cannot Approve","Only pending orders can be approved."); return; }
                    po.setStatus("Approved"); table.refresh();
                    addActivity("Purchase Order " + po.getId() + " approved");
                });
                canBtn.setOnAction(e -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    if (po.getStatus().equals("Received")) { showError("Cannot Cancel","Received orders cannot be cancelled."); return; }
                    if (confirmDelete("cancel PO", po.getId())) { po.setStatus("Cancelled"); table.refresh(); }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(4, appBtn, rcvBtn, canBtn));
            }
        });

        table.getColumns().addAll(colId, colSup, colProd, colQty, colTot, colDate, colStat, colAct);
        addBtn.setOnAction(e -> showPurchaseDialog(table, filtered));
        page.getChildren().addAll(toolbar, createCard("", table));
        return page;
    }

    private void showPurchaseDialog(TableView<PurchaseOrder> table, ObservableList<PurchaseOrder> filtered) {
        Dialog<PurchaseOrder> dialog = new Dialog<>();
        dialog.setTitle("New Purchase Order");
        dialog.getDialogPane().setPrefWidth(460);
        styleDialog(dialog);
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        ComboBox<String> fSup = new ComboBox<>();
        suppliers.forEach(s -> fSup.getItems().add(s.getName()));
        if (!suppliers.isEmpty()) fSup.setValue(suppliers.get(0).getName());
        styleComboBox(fSup);

        ComboBox<String> fProd = new ComboBox<>();
        products.forEach(p -> fProd.getItems().add(p.getId() + " | " + p.getName()));
        if (!products.isEmpty()) fProd.setValue(products.get(0).getId() + " | " + products.get(0).getName());
        styleComboBox(fProd);

        TextField fQty   = styledField("10",  "Quantity");
        TextField fPrice = styledField("",     "Unit Price");
        TextField fDate  = styledField(today(),"Date");

        // Auto-fill price from product
        fProd.setOnAction(e -> {
            if (fProd.getValue() == null) return;
            String pid = fProd.getValue().split("\\|")[0].trim();
            products.stream().filter(p -> p.getId().equals(pid)).findFirst()
                    .ifPresent(p -> fPrice.setText(String.valueOf((int)p.getPurchasePrice())));
        });
        if (!products.isEmpty()) fPrice.setText(String.valueOf((int)products.get(0).getPurchasePrice()));

        grid.addRow(0, fieldLabel("Supplier"), fSup,   fieldLabel("Product"),    fProd);
        grid.addRow(1, fieldLabel("Quantity"), fQty,   fieldLabel("Unit Price"), fPrice);
        grid.addRow(2, fieldLabel("Date"),     fDate);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Create PO");
        styleDialogButton(okBtn, ACCENT);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fSup.getValue() == null || fProd.getValue() == null) { showError("Error","Select supplier and product."); return null; }
                try {
                    int qty    = Integer.parseInt(fQty.getText());
                    double up  = Double.parseDouble(fPrice.getText());
                    if (qty <= 0 || up < 0) { showError("Error","Values must be positive."); return null; }
                    String pid  = fProd.getValue().split("\\|")[0].trim();
                    String pnam = fProd.getValue().split("\\|")[1].trim();
                    PurchaseOrder po = new PurchaseOrder(fSup.getValue(), pid, pnam, qty, up, fDate.getText());
                    purchases.add(po);
                    filtered.setAll(purchases);
                    addActivity("Purchase Order " + po.getId() + " created for " + po.getProduct());
                    table.refresh();
                } catch (NumberFormatException ex) { showError("Error","Enter valid numbers."); return null; }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // EXPENSES MODULE
    // ============================================================
    private Node buildExpenses() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        TextField search = new TextField();
        search.setPromptText("🔍  Search expenses...");
        search.setPrefWidth(240);
        styleTextField(search);
        ComboBox<String> catFilter = new ComboBox<>();
        catFilter.getItems().addAll("All Categories","Utilities","Rent","Salaries","Transport","Marketing","Office","Maintenance","Other");
        catFilter.setValue("All Categories");
        styleComboBox(catFilter);
        Button addBtn = createPrimaryButton("+ Add Expense");
        HBox toolbar = new HBox(12, search, catFilter, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        // Summary
        HBox summary = new HBox(12);
        summary.getChildren().addAll(
            createSmallStat("Total Expenses",    fmt(getTotalExpenses()),                  DANGER),
            createSmallStat("This Month",        fmt(expenses.stream().filter(e->e.getDate().contains("Sep")).mapToDouble(Expense::getAmount).sum()), WARNING),
            createSmallStat("Total Transactions",String.valueOf(expenses.size()),           ACCENT),
            createSmallStat("Avg per Transaction",fmt(expenses.isEmpty()?0:getTotalExpenses()/expenses.size()), PURPLE)
        );

        ObservableList<Expense> filtered = FXCollections.observableArrayList(expenses);
        TableView<Expense> table = new TableView<>(filtered);
        styleTable(table);
        table.setPrefHeight(440);

        TableColumn<Expense,String> colId   = col("Exp ID",    "id",             90);
        TableColumn<Expense,String> colTit  = col("Title",     "title",          180);
        TableColumn<Expense,String> colCat  = col("Category",  "category",       120);
        TableColumn<Expense,Number> colAmt  = numCol("Amount", "amount",         120);
        TableColumn<Expense,String> colDate = col("Date",      "date",           120);
        TableColumn<Expense,String> colPay  = col("Payment",   "paymentMethod",  120);
        TableColumn<Expense,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(140);

        colAmt.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt(item.doubleValue()));
            }
        });
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = createSmallButton("Edit",   ACCENT);
            private final Button delBtn  = createSmallButton("Delete", DANGER);
            {
                editBtn.setOnAction(e -> { Expense ex = getTableView().getItems().get(getIndex()); showExpenseDialog(ex, table, filtered, summary); });
                delBtn.setOnAction(e -> {
                    Expense ex = getTableView().getItems().get(getIndex());
                    if (confirmDelete("expense", ex.getTitle())) {
                        expenses.remove(ex);
                        filtered.setAll(applyExpFilter(search.getText(), catFilter.getValue()));
                        refreshSummaryStats(summary);
                        refreshDashboardKPIs();
                    }
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(6, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colId, colTit, colCat, colAmt, colDate, colPay, colAct);
        search.textProperty().addListener((o,ov,nv) -> filtered.setAll(applyExpFilter(nv, catFilter.getValue())));
        catFilter.setOnAction(e -> filtered.setAll(applyExpFilter(search.getText(), catFilter.getValue())));
        addBtn.setOnAction(e -> showExpenseDialog(null, table, filtered, summary));

        page.getChildren().addAll(toolbar, createCard("Expense Summary", summary), createCard("", table));
        return page;
    }

    private List<Expense> applyExpFilter(String q, String cat) {
        q = q.toLowerCase();
        final String fq = q, fc = cat;
        return expenses.stream().filter(e ->
            (e.getTitle().toLowerCase().contains(fq) || e.getCategory().toLowerCase().contains(fq)) &&
            (fc.equals("All Categories") || e.getCategory().equals(fc))
        ).toList();
    }

    private void refreshSummaryStats(HBox summary) {
        // Not easily updatable in-place without refs; navigating away and back refreshes
    }

    private void showExpenseDialog(Expense exp, TableView<Expense> table, ObservableList<Expense> filtered, HBox summary) {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle(exp == null ? "Add Expense" : "Edit Expense");
        dialog.getDialogPane().setPrefWidth(460);
        styleDialog(dialog);
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField fTitle = styledField(exp != null ? exp.getTitle()       : "", "Title");
        ComboBox<String> fCat = new ComboBox<>();
        fCat.getItems().addAll("Utilities","Rent","Salaries","Transport","Marketing","Office","Maintenance","Other");
        fCat.setValue(exp != null ? exp.getCategory() : "Office");
        styleComboBox(fCat);
        TextField fAmt  = styledField(exp != null ? String.valueOf((int)exp.getAmount())  : "", "Amount");
        TextField fDate = styledField(exp != null ? exp.getDate()         : today(), "Date");
        TextField fDesc = styledField(exp != null ? exp.getDescription()  : "", "Description");
        ComboBox<String> fPay = new ComboBox<>();
        fPay.getItems().addAll("Cash","Bank Transfer","Online","Cheque","Credit Card");
        fPay.setValue(exp != null ? exp.getPaymentMethod() : "Cash");
        styleComboBox(fPay);

        grid.addRow(0, fieldLabel("Title"),          fTitle, fieldLabel("Category"),       fCat);
        grid.addRow(1, fieldLabel("Amount"),         fAmt,   fieldLabel("Date"),           fDate);
        grid.addRow(2, fieldLabel("Description"),    fDesc,  fieldLabel("Payment Method"), fPay);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(exp == null ? "Add Expense" : "Save");
        styleDialogButton(okBtn, ACCENT);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (fTitle.getText().isBlank() || fAmt.getText().isBlank()) { showError("Error","Title and Amount are required."); return null; }
                double amt;
                try { amt = Double.parseDouble(fAmt.getText()); }
                catch (NumberFormatException ex) { showError("Error","Enter a valid amount."); return null; }
                if (amt < 0) { showError("Error","Amount cannot be negative."); return null; }
                if (exp == null) {
                    Expense ne = new Expense(fTitle.getText(), fCat.getValue(), amt, fDate.getText(), fDesc.getText(), fPay.getValue());
                    expenses.add(ne);
                    addActivity("Expense '" + ne.getTitle() + "' of " + fmt(amt) + " recorded");
                } else {
                    exp.setTitle(fTitle.getText()); exp.setCategory(fCat.getValue());
                    exp.setAmount(amt); exp.setDate(fDate.getText());
                    exp.setDescription(fDesc.getText()); exp.setPaymentMethod(fPay.getValue());
                }
                filtered.setAll(expenses);
                refreshDashboardKPIs();
                table.refresh();
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ============================================================
    // PAYROLL MODULE
    // ============================================================
    private Node buildPayroll() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Summary
        HBox summary = new HBox(12);
        double totalNet  = payrolls.stream().mapToDouble(PayrollRecord::getNetSalary).sum();
        long   pending   = payrolls.stream().filter(p -> p.getPaymentStatus().equals("Pending")).count();
        long   paid      = payrolls.stream().filter(p -> p.getPaymentStatus().equals("Paid")).count();
        summary.getChildren().addAll(
            createSmallStat("Total Payroll",   fmt(totalNet), ACCENT),
            createSmallStat("Paid",            String.valueOf(paid),    SUCCESS),
            createSmallStat("Pending",         String.valueOf(pending), WARNING),
            createSmallStat("Total Employees", String.valueOf(payrolls.size()), PURPLE)
        );

        // Calculator panel
        VBox calcPanel = new VBox(12);
        calcPanel.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 12; " +
                           "-fx-border-color: " + getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 20;");
        Label calcTitle = new Label("Salary Calculator");
        calcTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");

        GridPane calcGrid = new GridPane();
        calcGrid.setHgap(16); calcGrid.setVgap(12);

        TextField fBasic = styledField("80000", "Basic Salary");
        TextField fBonus = styledField("8000",  "Bonus");
        TextField fTax   = styledField("4000",  "Tax");
        TextField fDedu  = styledField("1000",  "Other Deductions");
        Label netLbl     = new Label(fmt(0));
        netLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + SUCCESS + ";");

        Runnable recalcSal = () -> {
            try {
                double basic  = Double.parseDouble(fBasic.getText().replace(",",""));
                double bonus  = Double.parseDouble(fBonus.getText().replace(",",""));
                double tax    = Double.parseDouble(fTax.getText().replace(",",""));
                double dedu   = Double.parseDouble(fDedu.getText().replace(",",""));
                double net    = basic + bonus - tax - dedu;
                netLbl.setText(fmt(net));
                netLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + (net >= 0 ? SUCCESS : DANGER) + ";");
            } catch (NumberFormatException ignore) {}
        };
        fBasic.textProperty().addListener((o,ov,nv) -> recalcSal.run());
        fBonus.textProperty().addListener((o,ov,nv) -> recalcSal.run());
        fTax.textProperty().addListener((o,ov,nv)   -> recalcSal.run());
        fDedu.textProperty().addListener((o,ov,nv)  -> recalcSal.run());
        recalcSal.run();

        calcGrid.addRow(0, fieldLabel("Basic Salary"), fBasic, fieldLabel("Bonus"),            fBonus);
        calcGrid.addRow(1, fieldLabel("Tax"),          fTax,   fieldLabel("Other Deductions"), fDedu);
        calcGrid.addRow(2, fieldLabel("NET SALARY"),   netLbl);
        calcPanel.getChildren().addAll(calcTitle, new Separator(), calcGrid);

        // Table
        TableView<PayrollRecord> table = new TableView<>(payrolls);
        styleTable(table);
        table.setPrefHeight(380);

        TableColumn<PayrollRecord,String> colId   = col("Pay ID",    "id",             90);
        TableColumn<PayrollRecord,String> colName = col("Employee",  "employeeName",   160);
        TableColumn<PayrollRecord,String> colDept = col("Department","department",     150);
        TableColumn<PayrollRecord,Number> colBasic= numCol("Basic",  "basicSalary",   110);
        TableColumn<PayrollRecord,Number> colBonus= numCol("Bonus",  "bonus",          90);
        TableColumn<PayrollRecord,Number> colNet  = numCol("Net Sal","netSalary",     120);
        TableColumn<PayrollRecord,String> colStat = col("Status",    "paymentStatus",  90);
        TableColumn<PayrollRecord,Void>   colAct  = new TableColumn<>("Actions");
        colAct.setPrefWidth(120);

        for (TableColumn<PayrollRecord,Number> col : new TableColumn[]{colBasic, colBonus, colNet}) {
            col.setCellFactory(c -> new TableCell<>() {
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : fmt(item.doubleValue()));
                }
            });
        }
        colStat.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(item);
                String bg = item.equals("Paid") ? SUCCESS : WARNING;
                lbl.setStyle("-fx-background-color: " + bg + "22; -fx-text-fill: " + bg +
                             "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });
        colAct.setCellFactory(c -> new TableCell<>() {
            private final Button payBtn = createSmallButton("Pay Now", SUCCESS);
            {
                payBtn.setOnAction(e -> {
                    PayrollRecord pr = getTableView().getItems().get(getIndex());
                    if (pr.getPaymentStatus().equals("Paid")) { showError("Already Paid","Salary already processed."); return; }
                    pr.setPaymentStatus("Paid");
                    pr.setPaymentDate(today());
                    table.refresh();
                    refreshDashboardKPIs();
                    addActivity("Salary of " + fmt(pr.getNetSalary()) + " paid to " + pr.getEmployeeName());
                    addNotification("success","Salary Paid","Salary processed for " + pr.getEmployeeName());
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : payBtn);
            }
        });

        table.getColumns().addAll(colId, colName, colDept, colBasic, colBonus, colNet, colStat, colAct);

        Button processAllBtn = createButton("💵  Process All Pending", WARNING);
        processAllBtn.setOnAction(e -> {
            long countPending = payrolls.stream().filter(p -> p.getPaymentStatus().equals("Pending")).count();
            if (countPending == 0) { showError("No Pending","All salaries are already processed."); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.setTitle("Confirm Payroll"); conf.setHeaderText(null);
            conf.setContentText("Process salary for all " + countPending + " pending employees?");
            conf.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    payrolls.stream().filter(p -> p.getPaymentStatus().equals("Pending")).forEach(p -> {
                        p.setPaymentStatus("Paid"); p.setPaymentDate(today());
                    });
                    table.refresh(); refreshDashboardKPIs();
                    addActivity("Bulk payroll processed for " + countPending + " employees");
                    addNotification("success","Payroll Complete", countPending + " salaries processed.");
                    showSuccess("Payroll Processed", countPending + " salaries paid successfully.");
                }
            });
        });

        HBox payrollToolbar = new HBox(12, processAllBtn);
        payrollToolbar.setAlignment(Pos.CENTER_RIGHT);

        page.getChildren().addAll(createCard("Payroll Overview", summary), calcPanel, payrollToolbar, createCard("Payroll Records", table));
        return page;
    }

    // ============================================================
    // REPORTS MODULE
    // ============================================================
    private Node buildReports() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Filter bar
        ComboBox<String> period = new ComboBox<>();
        period.getItems().addAll("Today","This Week","This Month","All Time");
        period.setValue("This Month");
        styleComboBox(period);
        Button genBtn = createPrimaryButton("Generate Report");
        HBox toolbar  = new HBox(12, new Label("Period:") {{ setStyle("-fx-text-fill:" + SmartBizERP.this.getText() + ";"); }}, period, genBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Sales Report
        VBox salesReport = buildSalesReport();
        // Inventory Report
        VBox invReport   = buildInventoryReport();
        // Expense Report
        VBox expReport   = buildExpenseReport();
        // Attendance Report
        VBox attReport   = buildAttendanceReport();

        genBtn.setOnAction(e -> showSuccess("Report Generated","Report generated for: " + period.getValue()));

        HBox row1 = new HBox(16, salesReport, invReport);
        HBox row2 = new HBox(16, expReport, attReport);
        HBox.setHgrow(salesReport, Priority.ALWAYS);
        HBox.setHgrow(invReport,   Priority.ALWAYS);
        HBox.setHgrow(expReport,   Priority.ALWAYS);
        HBox.setHgrow(attReport,   Priority.ALWAYS);

        page.getChildren().addAll(toolbar, row1, row2);
        return page;
    }

    private VBox buildSalesReport() {
        double totalSales  = getTotalSales();
        int    invoiceCount= sales.size();
        double avgInvoice  = invoiceCount == 0 ? 0 : totalSales / invoiceCount;
        Map<String, Double> prodSales = new HashMap<>();
        for (Sale s : sales) for (SaleItem si : s.items) prodSales.merge(si.productName, si.total, Double::sum);
        String topProd = prodSales.entrySet().stream().max(Map.Entry.comparingByValue())
                                  .map(Map.Entry::getKey).orElse("N/A");

        VBox card = createCard("Sales Report", buildReportRows(new String[][]{
            {"Total Sales",      fmt(totalSales), SUCCESS},
            {"Total Invoices",   String.valueOf(invoiceCount), ACCENT},
            {"Average Invoice",  fmt(avgInvoice), PURPLE},
            {"Top Product",      topProd, WARNING}
        }));
        return card;
    }

    private VBox buildInventoryReport() {
        return createCard("Inventory Report", buildReportRows(new String[][]{
            {"Total Products",    String.valueOf(products.size()), ACCENT},
            {"In Stock",         String.valueOf(products.stream().filter(p->p.getStatus().equals("IN STOCK")).count()), SUCCESS},
            {"Low Stock",        String.valueOf(products.stream().filter(p->p.getStatus().equals("LOW STOCK")).count()), WARNING},
            {"Out of Stock",     String.valueOf(products.stream().filter(p->p.getStatus().equals("OUT OF STOCK")).count()), DANGER},
            {"Inventory Value",  fmt(getInventoryValue()), PURPLE}
        }));
    }

    private VBox buildExpenseReport() {
        Map<String, Double> catExp = new HashMap<>();
        for (Expense e : expenses) catExp.merge(e.getCategory(), e.getAmount(), Double::sum);
        String topCat = catExp.entrySet().stream().max(Map.Entry.comparingByValue())
                               .map(Map.Entry::getKey).orElse("N/A");
        return createCard("Expense Report", buildReportRows(new String[][]{
            {"Total Expenses",   fmt(getTotalExpenses()), DANGER},
            {"Transactions",     String.valueOf(expenses.size()), ACCENT},
            {"Highest Category", topCat, WARNING},
            {"Avg per Expense",  fmt(expenses.isEmpty()?0:getTotalExpenses()/expenses.size()), PURPLE}
        }));
    }

    private VBox buildAttendanceReport() {
        long present = attendance.stream().filter(a->a.getStatus().equals("Present")||a.getStatus().equals("Checked Out")||a.getStatus().equals("Checked In")).count();
        long absent  = attendance.stream().filter(a->a.getStatus().equals("Absent")).count();
        long late    = attendance.stream().filter(a->a.getStatus().equals("Late")).count();
        long leave   = attendance.stream().filter(a->a.getStatus().equals("Leave")).count();
        long total   = attendance.size();
        double pct   = total == 0 ? 0 : (double) present / total * 100;
        return createCard("Attendance Report", buildReportRows(new String[][]{
            {"Present",          String.valueOf(present), SUCCESS},
            {"Absent",           String.valueOf(absent),  DANGER},
            {"Late",             String.valueOf(late),    WARNING},
            {"On Leave",         String.valueOf(leave),   ORANGE},
            {"Attendance Rate",  String.format("%.1f%%", pct), ACCENT}
        }));
    }

    private VBox buildReportRows(String[][] rows) {
        VBox box = new VBox(8);
        for (String[] row : rows) {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER_LEFT);
            line.setStyle("-fx-border-color: transparent transparent " + getBorder() + " transparent; -fx-border-width: 0 0 1 0; -fx-padding: 6 0;");
            Label key = new Label(row[0]);
            key.setStyle("-fx-text-fill: " + getSub() + "; -fx-font-size: 12px;");
            HBox.setHgrow(key, Priority.ALWAYS);
            Label val = new Label(row[1]);
            val.setStyle("-fx-text-fill: " + row[2] + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            line.getChildren().addAll(key, val);
            box.getChildren().add(line);
        }
        return box;
    }

    // ============================================================
    // NOTIFICATIONS MODULE
    // ============================================================
    private Node buildNotifications() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button markAllBtn = createButton("Mark All Read", ACCENT);
        Button clearBtn   = createSmallButton("Clear All", DANGER);
        toolbar.getChildren().addAll(markAllBtn, clearBtn);

        VBox notifList = new VBox(8);
        Runnable renderNotifs = () -> {
            notifList.getChildren().clear();
            for (Notification n : notifications) {
                HBox card = new HBox(16);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(16));
                String cardBg = n.read ? getCard() : (darkMode ? "#1E2840" : "#EBF5FF");
                card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 10; " +
                              "-fx-border-color: " + getBorder() + "; -fx-border-radius: 10; -fx-border-width: 1;");

                String icon = switch(n.type) {
                    case "warning" -> "⚠"; case "success" -> "✅"; case "info" -> "ℹ"; default -> "🔔";
                };
                String iconColor = switch(n.type) {
                    case "warning" -> WARNING; case "success" -> SUCCESS; default -> ACCENT;
                };
                Label iconLbl = new Label(icon);
                iconLbl.setStyle("-fx-font-size: 20px; -fx-text-fill: " + iconColor + "; " +
                                 "-fx-min-width: 36px; -fx-min-height: 36px; -fx-alignment: center; " +
                                 "-fx-background-color: " + iconColor + "22; -fx-background-radius: 8;");

                VBox txt = new VBox(2);
                HBox.setHgrow(txt, Priority.ALWAYS);
                Label titleLbl = new Label(n.title);
                titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + getText() + ";");
                Label msgLbl   = new Label(n.message);
                msgLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + ";");
                msgLbl.setWrapText(true);
                Label timeLbl  = new Label(n.time);
                timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getSub() + ";");
                txt.getChildren().addAll(titleLbl, msgLbl, timeLbl);

                if (!n.read) {
                    Circle dot = new Circle(4, Color.web(ACCENT));
                    card.getChildren().addAll(iconLbl, txt, dot);
                } else {
                    card.getChildren().addAll(iconLbl, txt);
                }
                card.setOnMouseClicked(e -> { n.read = true; updateNotifBadge(); Platform.runLater(() -> { notifList.getChildren().clear(); }); });
                notifList.getChildren().add(card);
            }
        };
        renderNotifs.run();

        markAllBtn.setOnAction(e -> { notifications.forEach(n -> n.read = true); updateNotifBadge(); renderNotifs.run(); });
        clearBtn.setOnAction(e -> {
            if (confirmDelete("clear all", "notifications")) {
                notifications.clear(); updateNotifBadge(); renderNotifs.run();
            }
        });

        ScrollPane scroll = new ScrollPane(notifList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scroll.setPrefHeight(600);

        page.getChildren().addAll(toolbar, scroll);
        return page;
    }

    // ============================================================
    // SETTINGS MODULE
    // ============================================================
    private Node buildSettings() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));

        // Company Info
        VBox companyCard = createCard("Company Information", new VBox());
        GridPane compGrid = new GridPane();
        compGrid.setHgap(16); compGrid.setVgap(12);
        TextField fCompName  = styledField(companyName, "Company Name");
        TextField fOwner     = styledField(ownerName,   "Owner Name");
        TextField fEmail     = styledField(companyEmail,"Email");
        TextField fPhone     = styledField(companyPhone,"Phone");
        TextField fAddress   = styledField(companyAddr, "Address");
        ComboBox<String> fCurr = new ComboBox<>();
        fCurr.getItems().addAll("Rs.","USD ($)","EUR (€)","GBP (£)","AED");
        fCurr.setValue(currency);
        styleComboBox(fCurr);
        compGrid.addRow(0, fieldLabel("Company Name"), fCompName, fieldLabel("Owner"),   fOwner);
        compGrid.addRow(1, fieldLabel("Email"),        fEmail,    fieldLabel("Phone"),   fPhone);
        compGrid.addRow(2, fieldLabel("Address"),      fAddress,  fieldLabel("Currency"),fCurr);
        Button saveCompBtn = createPrimaryButton("Save Company Info");
        saveCompBtn.setOnAction(e -> {
            companyName  = fCompName.getText(); ownerName   = fOwner.getText();
            companyEmail = fEmail.getText();     companyPhone= fPhone.getText();
            companyAddr  = fAddress.getText();   currency    = fCurr.getValue();
            showSuccess("Settings Saved","Company information updated successfully.");
            addActivity("Company settings updated");
        });
        VBox compContent = new VBox(12, compGrid, saveCompBtn);
        ((VBox)companyCard.getChildren().get(companyCard.getChildren().size()-1)).getChildren().addAll(compGrid, saveCompBtn);
        companyCard.getChildren().remove(companyCard.getChildren().size()-1);
        companyCard.getChildren().add(compContent);

        // Theme Settings
        VBox themeCard = createCard("Appearance & Theme", new VBox());
        HBox themeRow = new HBox(16);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Current Theme:");
        themeLabel.setStyle("-fx-text-fill: " + getText() + "; -fx-font-size: 13px;");
        Button lightBtn = createButton("☀  Light Mode", darkMode ? getSub() : ACCENT);
        Button darkBtn  = createButton("🌙  Dark Mode",  darkMode ? ACCENT : getSub());
        lightBtn.setOnAction(e -> { darkMode = false; applyTheme(); navigateTo("Settings"); });
        darkBtn.setOnAction(e ->  { darkMode = true;  applyTheme(); navigateTo("Settings"); });
        themeRow.getChildren().addAll(themeLabel, lightBtn, darkBtn);
        ((VBox)themeCard.getChildren().get(themeCard.getChildren().size()-1)).getChildren().add(themeRow);
        themeCard.getChildren().remove(themeCard.getChildren().size()-1);
        themeCard.getChildren().add(themeRow);

        // User Info
        VBox userCard = createCard("User Account", new VBox());
        GridPane userGrid = new GridPane();
        userGrid.setHgap(16); userGrid.setVgap(12);
        TextField fUName = styledField("Administrator","Username");
        TextField fURole = styledField("Super Admin","Role");
        fURole.setEditable(false);
        userGrid.addRow(0, fieldLabel("Username"), fUName, fieldLabel("Role"), fURole);
        Button saveUserBtn = createPrimaryButton("Update User Info");
        saveUserBtn.setOnAction(e -> showSuccess("Updated","User information saved."));
        VBox userContent = new VBox(12, userGrid, saveUserBtn);
        ((VBox)userCard.getChildren().get(userCard.getChildren().size()-1)).getChildren().addAll(userGrid, saveUserBtn);
        userCard.getChildren().remove(userCard.getChildren().size()-1);
        userCard.getChildren().add(userContent);

        page.getChildren().addAll(buildCompanyCard(fCompName, fOwner, fEmail, fPhone, fAddress, fCurr),
                                   buildThemeCard(),
                                   buildUserCard());
        return page;
    }

    private VBox buildCompanyCard(TextField fCompName, TextField fOwner, TextField fEmail,
                                  TextField fPhone, TextField fAddress, ComboBox<String> fCurr) {
        VBox card = createCard("Company Information", new VBox());
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        grid.addRow(0, fieldLabel("Company Name"), fCompName, fieldLabel("Owner"),   fOwner);
        grid.addRow(1, fieldLabel("Email"),        fEmail,    fieldLabel("Phone"),   fPhone);
        grid.addRow(2, fieldLabel("Address"),      fAddress,  fieldLabel("Currency"),fCurr);
        Button saveBtn = createPrimaryButton("Save Company Info");
        saveBtn.setOnAction(e -> {
            companyName  = fCompName.getText(); ownerName   = fOwner.getText();
            companyEmail = fEmail.getText();     companyPhone= fPhone.getText();
            companyAddr  = fAddress.getText();   currency    = fCurr.getValue();
            showSuccess("Settings Saved","Company information updated.");
        });
        card.getChildren().remove(card.getChildren().size()-1);
        card.getChildren().addAll(grid, saveBtn);
        return card;
    }

    private VBox buildThemeCard() {
        VBox card = createCard("Appearance & Theme", new VBox());
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Select Theme:");
        lbl.setStyle("-fx-text-fill: " + getText() + "; -fx-font-size: 13px;");

        VBox lightOpt = createThemeOption("☀  Light Mode", "Clean and bright professional look", !darkMode);
        VBox darkOpt  = createThemeOption("🌙  Dark Mode",  "Easy on eyes in low light",           darkMode);

        lightOpt.setOnMouseClicked(e -> { if (darkMode) { darkMode = false; applyTheme(); navigateTo("Settings"); }});
        darkOpt.setOnMouseClicked(e  -> { if (!darkMode){ darkMode = true;  applyTheme(); navigateTo("Settings"); }});

        row.getChildren().addAll(lightOpt, darkOpt);
        card.getChildren().remove(card.getChildren().size()-1);
        card.getChildren().add(row);
        return card;
    }

    private VBox createThemeOption(String title, String desc, boolean selected) {
        VBox box = new VBox(6);
        box.setPrefWidth(200);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: " + (selected ? ACCENT + "22" : getCard()) + "; " +
                     "-fx-background-radius: 10; -fx-border-color: " + (selected ? ACCENT : getBorder()) + "; " +
                     "-fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;");
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (selected ? ACCENT : getText()) + ";");
        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + ";");
        d.setWrapText(true);
        box.getChildren().addAll(t, d);
        return box;
    }

    private VBox buildUserCard() {
        VBox card = createCard("User Account", new VBox());
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        TextField uName = styledField("Administrator","Username");
        TextField uRole = styledField("Super Admin","Role"); uRole.setEditable(false);
        grid.addRow(0, fieldLabel("Username"), uName, fieldLabel("Role"), uRole);
        Button saveBtn = createPrimaryButton("Update Account");
        saveBtn.setOnAction(e -> showSuccess("Updated","Account info saved."));
        card.getChildren().remove(card.getChildren().size()-1);
        card.getChildren().addAll(grid, saveBtn);
        return card;
    }

    private void applyTheme() {
        if (rootLayout != null) {
            rootLayout.setStyle("-fx-background-color: " + getBg() + ";");
            contentArea.setStyle("-fx-background-color: " + getBg() + ";");
        }
    }

    // ============================================================
    // THEME HELPERS
    // ============================================================
    private String getBg()     { return darkMode ? D_BG     : L_BG;     }
    private String getCard()   { return darkMode ? D_CARD   : L_CARD;   }
    private String getText()   { return darkMode ? D_TEXT   : L_TEXT;   }
    private String getSub()    { return darkMode ? D_SUB    : L_SUB;    }
    private String getBorder() { return darkMode ? D_BORDER : L_BORDER; }
    private String getHeader() { return darkMode ? D_HEADER : L_HEADER; }

    // ============================================================
    // SHARED UI HELPER METHODS
    // ============================================================
    private <S, T> TableColumn<S, T> col(String header, String property, double width) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }
    private <S> TableColumn<S, Number> numCol(String header, String property, double width) {
        TableColumn<S, Number> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    private void styleTable(TableView<?> table) {
        table.setStyle("-fx-background-color: " + getCard() + "; -fx-border-color: " + getBorder() +
                       "; -fx-border-width: 1; -fx-border-radius: 8;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label placeholder = new Label("No records found.");
        placeholder.setStyle("-fx-text-fill: " + getSub() + "; -fx-font-size: 13px;");
        table.setPlaceholder(placeholder);
    }

    private void styleTextField(TextField tf) {
        tf.setStyle("-fx-background-color: " + getCard() + "; -fx-border-color: " + getBorder() +
                    "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; " +
                    "-fx-font-size: 13px; -fx-text-fill: " + getText() + ";");
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: " + getCard() + "; -fx-border-color: " + getBorder() +
                    "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 8; " +
                    "-fx-font-size: 13px;");
    }

    private TextField styledField(String val, String prompt) {
        TextField tf = new TextField(val);
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: " + getCard() + "; -fx-border-color: " + getBorder() +
                    "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; " +
                    "-fx-font-size: 13px; -fx-text-fill: " + getText() + ";");
        tf.setPrefWidth(180);
        return tf;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getSub() + "; -fx-font-weight: bold; -fx-padding: 0 0 0 0;");
        lbl.setAlignment(Pos.CENTER_LEFT);
        GridPane.setValignment(lbl, VPos.BOTTOM);
        return lbl;
    }

    private Button createPrimaryButton(String label) {
        return createButton(label, ACCENT);
    }

    private Button createButton(String label, String color) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 12px; " +
                     "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        return btn;
    }

    private Button createSmallButton(String label, String color) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; -fx-font-size: 11px; " +
                     "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 10; -fx-cursor: hand; " +
                     "-fx-border-color: " + color + "44; -fx-border-radius: 4;");
        return btn;
    }

    private HBox createSmallStat(String label, String value, String color) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color: " + getCard() + "; -fx-background-radius: 10; " +
                     "-fx-border-color: " + color + "33; -fx-border-radius: 10; -fx-border-width: 1; " +
                     "-fx-padding: 12 20;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getSub() + ";");
        box.getChildren().addAll(v, l);
        HBox.setHgrow(box, Priority.ALWAYS);
        HBox wrapper = new HBox(box);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane dp = dialog.getDialogPane();
        dp.setStyle("-fx-background-color: " + getCard() + ";");
    }

    private void styleDialogButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                     "-fx-background-radius: 6; -fx-padding: 8 20;");
    }

    private boolean confirmDelete(String type, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + type + " '" + name + "'?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        launch(args);
    }
}