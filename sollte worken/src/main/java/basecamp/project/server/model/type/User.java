package basecamp.project.server.model.type;

public class User {

    private String firstName;
    private String lastName;

    public User() {
        this.firstName = "Test";
        this.lastName = "Lastname";
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

}
