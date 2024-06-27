import java.util.UUID;

public class Actor {
    private String actorId;

    private String actorName;

    private String actorDateOfBirthAsString = null;
    private int actorDateOfBirth = -1;

    public String getActorName() { return actorName; }

    public String getActorDateOfBirthAsString() { return actorDateOfBirthAsString; }

    public int getActorDateOfBirth() { return actorDateOfBirth; }

    public String getActorId() { return actorId; }

    public void setActorName(String actorName) { this.actorName = actorName; }

    public void setActorDateOfBirth(String dateOfBirth) {
        try {
            this.actorDateOfBirth = Integer.parseInt(dateOfBirth);
            this.actorDateOfBirthAsString = dateOfBirth;
        } catch (Exception e) {
            this.actorDateOfBirthAsString = null;
            throw e;
        }
    }

    public void generateActorId() {
        String idRandomFirstHalf = UUID.randomUUID().toString().substring(0, 7);

        // Combine actor's name and date of birth year
        String combinedInfo = actorName + (actorDateOfBirthAsString != null ? actorDateOfBirth : "");
        String idFromBytesSecondHalf = UUID.nameUUIDFromBytes(combinedInfo.getBytes()).toString().substring(12, 13);

        this.actorId = idRandomFirstHalf + idFromBytesSecondHalf;
    }

    @Override
    public String toString() {
        return String.format("ACTOR - ID: %s, Name: %s, DOB: %s", actorId, actorName, actorDateOfBirthAsString);
    }
}
