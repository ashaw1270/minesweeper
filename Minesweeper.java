import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Minesweeper {
    private static final int FRAME_HEIGHT = 700;
    private static final int FRAME_WIDTH = 700;
    private static final int GAME_HEIGHT = 10;
    private static final int GAME_WIDTH = 10;
    private static final int NUMBER_OF_BOMBS = 15;

    private final ArrayList<ArrayList<Integer>> bombs;
    private final HashMap<ArrayList<Integer>, Integer> nums;
    private final JPanel[][] panels;

    public Minesweeper() {
        bombs = new ArrayList<>();
        nums = new HashMap<>();
        panels = new JPanel[GAME_HEIGHT][GAME_WIDTH];

        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(GAME_HEIGHT, GAME_WIDTH));

        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                panel.setVisible(true);
                panel.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        ArrayList<Integer> loc = locationOf(panel);
                        if (((JLabel) panel.getComponent(0)).getText().equals("")) {
                            if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
                                if (panel.getBackground() == Color.GREEN) {
                                    panel.setBackground(Color.WHITE);
                                } else {
                                    panel.setBackground(Color.GREEN);
                                }
                            } else if (bombAt(loc)) {
                                panel.setBackground(Color.RED);
                                System.out.println("Dead");
                                explode(loc);
                            } else {
                                uncover(loc);
                            }
                        }
                    }
                });

                JLabel label = new JLabel();
                label.setFont(new Font("Arial", Font.PLAIN, FRAME_HEIGHT / GAME_HEIGHT / 2));
                label.setSize(FRAME_WIDTH / GAME_WIDTH, FRAME_HEIGHT / GAME_HEIGHT);
                label.setVisible(true);
                panel.add(label);

                panels[row][col] = panel;
                frame.add(panel);
            }
        }

        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setBackground(Color.BLACK);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        generateBombs();
        generateNums();

        //drawSquare(new ArrayList<>(List.of(4, 8)), 5);
    }

    public void explode(ArrayList<Integer> location) {
        drawSquare(location, 3);
        try {Thread.sleep(10000);} catch (InterruptedException ignored) {}
        for (int i = 5; !boardIsEmpty(); i += 2) {
            if (i % 4 > 1) {
                clear();
            }
            drawSquare(location, i);
            try {Thread.sleep(50);} catch (InterruptedException ignored) {}
        }
    }

    public boolean boardIsEmpty() {
        for (int row = 0; row < panels.length; row++) {
            for (int col = 0; col < panels[0].length; col++) {
                if (panels[row][col].getBackground() != Color.WHITE) {
                    return false;
                }
            }
        }
        return true;
    }

    public void clear() {
        for (int row = 0; row < panels.length; row++) {
            for (int col = 0; col < panels[0].length; col++) {
                setPanelColor(new ArrayList<>(List.of(row, col)), Color.WHITE);
            }
        }
    }

    public void drawSquare(ArrayList<Integer> center, int sideLength) {
        // top side
        for (int i = 0; i < sideLength; i++) {
            ArrayList<Integer> location = new ArrayList<>(List.of(center.get(0) - sideLength / 2, center.get(1) - sideLength / 2 + i));
            try {setPanelColor(location, Color.RED);} catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        // right side
        for (int i = 0; i < sideLength; i++) {
            ArrayList<Integer> location = new ArrayList<>(List.of(center.get(0) - sideLength / 2 + i, center.get(1) + sideLength / 2));
            try {setPanelColor(location, Color.RED);} catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        // bottom side
        for (int i = 0; i < sideLength; i++) {
            ArrayList<Integer> location = new ArrayList<>(List.of(center.get(0) + sideLength / 2, center.get(1) - sideLength / 2 + i));
            try {setPanelColor(location, Color.RED);} catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        // left side
        for (int i = 0; i < sideLength; i++) {
            ArrayList<Integer> location = new ArrayList<>(List.of(center.get(0) - sideLength / 2 + i, center.get(1) - sideLength / 2));
            try {setPanelColor(location, Color.RED);} catch (ArrayIndexOutOfBoundsException ignored) {}
        }
    }

    public void setPanelColor(ArrayList<Integer> location, Color color) {
        panels[location.get(0)][location.get(1)].setBackground(color);
    }

    public void uncover(ArrayList<Integer> loc) {
        JPanel panel = panels[loc.get(0)][loc.get(1)];
        panel.setBackground(new Color(185, 185, 185));
        JLabel label = (JLabel) panel.getComponent(0);
        int bombsTouching = nums.get(loc);
        if (bombsTouching > 0) {
            label.setText(String.valueOf(bombsTouching));
        } else {
            return;
        }
        label.setForeground(switch (bombsTouching) {
            case 1 -> new Color(0, 0, 255);
            case 2 -> new Color(0, 130, 0);
            case 3 -> new Color(255, 0, 0);
            case 4 -> new Color(0, 0, 130);
            case 5 -> new Color(130, 0, 0);
            case 6 -> new Color(0, 130, 130);
            case 7 -> new Color(130, 0, 130);
            // case 8
            default -> new Color(117, 117, 117);
        });
    }

    public ArrayList<Integer> locationOf(JPanel panel) {
        for (int row = 0; row < panels.length; row++) {
            for (int col = 0; col < panels[0].length; col++) {
                if (panels[row][col] == panel) {
                    return new ArrayList<>(List.of(row, col));
                }
            }
        }
        return null;
    }

    public void generateBombs() {
        ArrayList<ArrayList<Integer>> rands = new ArrayList<>();
        while (rands.size() < NUMBER_OF_BOMBS) {
            int row = (int) (Math.random() * GAME_HEIGHT);
            int col = (int) (Math.random() * GAME_WIDTH);
            ArrayList<Integer> point = new ArrayList<>(List.of(row, col));
            if (!rands.contains(point)) {
                rands.add(point);
            }
        }
        bombs.addAll(rands);
    }

    public void generateNums() {
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                ArrayList<Integer> point = new ArrayList<>(List.of(row, col));
                if (!bombAt(point)) {
                    nums.put(point, bombsTouching(point));
                }
            }
        }

        for (Map.Entry<ArrayList<Integer>, Integer> entry : nums.entrySet()) {
            if (entry.getValue() == 0) {
                panels[entry.getKey().get(0)][entry.getKey().get(1)].setBackground(new Color(185, 185, 185));
            }
        }
    }

    public int bombsTouching(ArrayList<Integer> point) {
        int bombsTouching = 0;

        ArrayList<ArrayList<Integer>> points = new ArrayList<>(List.of(
                new ArrayList<>(List.of(point.get(0) - 1, point.get(1) - 1)),
                new ArrayList<>(List.of(point.get(0) - 1, point.get(1))),
                new ArrayList<>(List.of(point.get(0) - 1, point.get(1) + 1)),
                new ArrayList<>(List.of(point.get(0), point.get(1) - 1)),
                new ArrayList<>(List.of(point.get(0), point.get(1) + 1)),
                new ArrayList<>(List.of(point.get(0) + 1, point.get(1) - 1)),
                new ArrayList<>(List.of(point.get(0) + 1, point.get(1))),
                new ArrayList<>(List.of(point.get(0) + 1, point.get(1) + 1))
        ));

        for (ArrayList<Integer> loc : points) {
            try {
                if (bombAt(loc)) {
                    bombsTouching++;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        return bombsTouching;
    }

    public boolean bombAt(ArrayList<Integer> loc) {
        return bombs.contains(loc);
    }

    public static void main(String[] args) {
        new Minesweeper();
    }
}
