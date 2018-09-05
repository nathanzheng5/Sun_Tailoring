package AddressBook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactInfo {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6})", Pattern.CASE_INSENSITIVE);

    private String name;
    private Address address;
    private String phone;
    private String email;

    public ContactInfo(String name, Address address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public ContactInfo(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = new Address(name, "", "", "", "");
    }

    public static ContactInfo parse(String inputString) {
        String phoneString = "";
        String phoneStr1 = "";
        String phoneStr2 = "";
        String phoneStr3 = "";
        boolean matchedPhone = false;
        Matcher matcher = Pattern.compile("([0-9]{3})-([0-9]{3})-([0-9]{4})").matcher(inputString);
        if (matcher.find()) {
            phoneString = matcher.group(0);
            phoneStr1 = matcher.group(1);
            phoneStr2 = matcher.group(2);
            phoneStr3 = matcher.group(3);
            matchedPhone = true;
        } else {
            matcher = Pattern.compile("([0-9]{3})([0-9]{3})([0-9]{4})").matcher(inputString);
            if (matcher.find()) {
                phoneString = matcher.group(0);
                phoneStr1 = matcher.group(1);
                phoneStr2 = matcher.group(2);
                phoneStr3 = matcher.group(3);
                matchedPhone = true;
            }
        }


        String emailString = "";
        matcher = EMAIL_PATTERN.matcher(inputString);
        if (matcher.find()) {
            emailString = matcher.group(1);
        }

        Address address = new Address("", "", "", "", "");

        String customerName = inputString;
        if (!phoneString.isEmpty()) {
            customerName = customerName.replaceAll(phoneString, "");
        }
        if (!emailString.isEmpty()) {
            customerName = customerName.replaceAll(emailString, "");
        }
        customerName = customerName.replaceAll("\n", "");

        return new ContactInfo(customerName, address,
                (matchedPhone ? (phoneStr1 + "-" + phoneStr2 + "-" + phoneStr3) : ""),
                emailString);
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean similar(ContactInfo other) {
        return this.name.equalsIgnoreCase(other.name) &&
                (this.phone.replaceAll("-", "").equals(other.phone.replaceAll("-", "")) ||
                        this.email.equalsIgnoreCase(other.email));
    }
}
