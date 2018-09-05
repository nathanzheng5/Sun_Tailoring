package AddressBook;

import Lib.Persistable;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static InvoiceMaker.StLogger.logAction;

public class AddressBook implements Persistable {

    private static final long serialVersionUID = 1L;

    // todo: move into properties
    private static final String CSV_FILE_PATH = "./Settings/AddressBook.csv";
//    private static final String CSV_FILE_PATH = "./Settings/TestAddressBook.csv";
    private static final String ADDRESS_BOOK_CSV_FILE_HEADER = "Name,Address Name,Address Street,Address City,Address Province,Address Postal Code,Phone,Email,";
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),");

    private final List<ContactInfo> customerInfos;

    public AddressBook() {
        customerInfos = new ArrayList<>(100);
        load();
    }

    public int getNumEntries() {
        return customerInfos.size();
    }

    public ContactInfo getEntry(int i) {
        return customerInfos.get(i);
    }

    public void addEntry(ContactInfo contactInfo) {
        customerInfos.add(contactInfo);
    }

    public boolean contains(ContactInfo contactInfo) {
        return customerInfos.stream().anyMatch(customerInfo -> customerInfo.similar(contactInfo));
    }

    public void removeEntry(int i) {
        customerInfos.remove(i);
    }

    public List<ContactInfo> getCustomerInfos() {
        return Collections.unmodifiableList(customerInfos);
    }

    public void sort() {
        Collections.sort(customerInfos, (o1, o2) -> {
            // Sun Tailoring always stay on top
            if (o1.getName().equals("Sun Tailoring")) {
                return -1;
            }
            if (o2.getName().equals("Sun Tailoring")) {
                return 1;
            }

            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        });
    }

    @Override
    public boolean save() {
        SwingUtilities.invokeLater(() -> {
            sort();

            File file = new File(CSV_FILE_PATH);
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println(ADDRESS_BOOK_CSV_FILE_HEADER);
                for (ContactInfo contactInfo : customerInfos) {
                    pw.print(contactInfo.getName() + ",");
                    Address address = contactInfo.getAddress();
                    pw.print(address.getName() + ",");
                    pw.print(address.getStreet() + ",");
                    pw.print(address.getCity() + ",");
                    pw.print(address.getProvince() + ",");
                    pw.print(address.getPostalCode() + ",");
                    pw.print(contactInfo.getPhone() + ",");
                    pw.println(contactInfo.getEmail() + ",");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean load() {
        File file = new File(CSV_FILE_PATH);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = br.readLine().trim();
            if (line.equals(ADDRESS_BOOK_CSV_FILE_HEADER)) {
                line = br.readLine();
                while (line != null && !line.trim().isEmpty()) {
                    Matcher matcher = ADDRESS_PATTERN.matcher(line.trim());
                    if (matcher.matches()) {
                        String name = matcher.group(1).trim();
                        String addressName = matcher.group(2).trim();
                        String addressStreet = matcher.group(3).trim();
                        String addressCity = matcher.group(4).trim();
                        String addressProvince = matcher.group(5).trim();
                        String addressPostalCode = matcher.group(6).trim();
                        Address address = new Address(addressName, addressStreet, addressCity, addressProvince, addressPostalCode);
                        String phone = matcher.group(7).trim();
                        String email = matcher.group(8).trim();
                        ContactInfo contactInfo = new ContactInfo(name, address, phone,  email);
                        customerInfos.add(contactInfo);
                    }
                    line = br.readLine();
                }
            }

            logAction("Loaded address book");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
