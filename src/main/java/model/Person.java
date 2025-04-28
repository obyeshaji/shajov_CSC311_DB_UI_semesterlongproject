package model;

public class Person {
    private Integer id;
    private String firstName;
    private String lastName;
    private String department;
    private Major major;
    private String email;
    private String imageURL;
    
    public enum Major {
        CS("Computer Science"),
        CPIS("Computer Information Systems"),
        ENGLISH("English");
        
        private final String displayName;
        
        Major(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        public static Major fromString(String text) {
            for (Major major : Major.values()) {
                if (major.displayName.equalsIgnoreCase(text) || 
                    major.name().equalsIgnoreCase(text)) {
                    return major;
                }
            }
            return CS; // Default value
        }
    }

    public Person() {
    }

    public Person(String firstName, String lastName, String department, String majorStr, String email, String imageURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = Major.fromString(majorStr);
        this.email = email;
        this.imageURL = imageURL;
    }

    public Person(Integer id, String firstName, String lastName, String department, String majorStr, String email, String imageURL) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = Major.fromString(majorStr);
        this.email = email;
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }
    
    public void setMajor(String majorStr) {
        this.major = Major.fromString(majorStr);
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", major='" + major + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}