package lohvin;

public class DocWordPositions {
    private String id;
    private int[] positions;

    public DocWordPositions(int[] positions, String id) {
        this.positions = positions;
        this.id = id;
    }

    public int[] getPositions() {
        return positions;
    }

    public void setPositions(int[] positions) {
        this.positions = positions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
