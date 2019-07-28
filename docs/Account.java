package com.x.y;

import com.landawn.abacus.annotation.Type;
import com.landawn.abacus.util.N;
import java.sql.Date;
import java.util.Map;

/**
 * Generated by Abacus.
 * @version ${version}
 */
public class Account {

    private String firstName;
    private String lastName;
    private Date birthdate;
    private String email;
    private String address;
    private Map<String, Object> attrs;

    @Type("String")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Type("String")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Type("Date")
    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    @Type("String")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Type("String")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Type("Map<String, Object>")
    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + N.hashCode(firstName);
        h = 31 * h + N.hashCode(lastName);
        h = 31 * h + N.hashCode(birthdate);
        h = 31 * h + N.hashCode(email);
        h = 31 * h + N.hashCode(address);
        h = 31 * h + N.hashCode(attrs);

        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Account) {
            Account other = (Account) obj;

            if (N.equals(firstName, other.firstName)
                && N.equals(lastName, other.lastName)
                && N.equals(birthdate, other.birthdate)
                && N.equals(email, other.email)
                && N.equals(address, other.address)
                && N.equals(attrs, other.attrs)) {

                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "{firstName=" + N.toString(firstName)
                 + ", lastName=" + N.toString(lastName)
                 + ", birthdate=" + N.toString(birthdate)
                 + ", email=" + N.toString(email)
                 + ", address=" + N.toString(address)
                 + ", attrs=" + N.toString(attrs)
                 + "}";
    }
}