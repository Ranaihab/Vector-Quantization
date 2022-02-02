import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class gui {
    private JPanel panel1;
    private JTextField path;
    private JSpinner width;
    private JSpinner height;
    private JSpinner size;
    private JButton compressButton;
    private JButton decompressButton;

    public gui() {
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VectorQuantization.vectorWidth = (int) width.getValue();
                VectorQuantization.vectorHeight = (int) height.getValue();
                VectorQuantization.codeBookSize = (int) size.getValue();
                VectorQuantization.compress(path.getText());
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VectorQuantization.decompress("decompressed.jpg");
            }
        });
    }

    public JPanel getPanel() {
        return panel1;
    }
}
