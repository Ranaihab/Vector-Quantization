import javax.swing.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class vector{
    private double[][] vectors;
    public vector(int x, int y){
        vectors = new double[x][y];
    }

    /*public void add(int i, double val){
        vectors.get(i).add(val);
    }

    public double getElement(int i, int j){
        return vectors.get(i).get(j);
    }*/

    public void add(int i, int j, double val){
        vectors[i][j] = val;
    }

    public double getElement(int i, int j){
        return vectors[i][j];
    }
}

public class VectorQuantization {
    public static int vectorHeight;
    public static int vectorWidth;
    public static int codeBookSize;
    public static ArrayList<vector> codeBook = new ArrayList<>();
    public static ArrayList<Integer> codes;
    public static int height;
    public static int width;

    public static int[][] scaleImage(int orHeight, int orWidth, int[][] image){
        int resizedHeight;
        int resizedWidth;
        if(orHeight % vectorHeight == 0) {
            resizedHeight = orHeight ;
        }
        else {
            resizedHeight = ((orHeight / vectorHeight) + 1)*vectorHeight;
        }

        if(orWidth % vectorWidth == 0) {
            resizedWidth = orWidth;
        }
        else {
            resizedWidth = ( (orWidth  /  vectorWidth) + 1) * vectorWidth;
        }

        int[][] resizedImage= new int[resizedHeight][resizedWidth];

        for (int i = 0; i < resizedHeight; i++) {
            int x = i;
            if(x >= orHeight) {
                x = orHeight - 1;
            }
            else {
                x = i;
            }
            for (int j = 0; j < resizedWidth; j++) {
                int y = j;
                if(y >= orWidth ) {
                    y = orWidth- 1;
                }
                else {
                    y = j;
                }
                resizedImage[i][j] = image[x][y];
            }
        }


        return resizedImage;
    }

    public static ArrayList<vector> createImageVectors(int[][]image){
        ArrayList<vector> imageVector = new ArrayList<>();
        for (int row = 0; row < (image.length); row += vectorHeight) {
            for (int column = 0; column < image[0].length; column += vectorWidth) {
                vector vec = new vector(vectorHeight, vectorWidth);

                for (int k = 0; k < vectorHeight; k++) {
                    for (int l = 0; l < vectorWidth; l++) {
                        vec.add(k, l, (double)image[row + k][column + l]);
                    }
                }
                imageVector.add(vec);
            }
        }

        return imageVector;
    }

    public static vector getAverage(ArrayList<vector> vectors){
        vector v = new vector(vectorHeight, vectorWidth);
        for(int i = 0; i<vectorHeight; i++){
            for (int j =0; j<vectorWidth; j++){
                double avg = 0;
                for(int x = 0; x<vectors.size(); x++){
                    avg+= vectors.get(x).getElement(i, j);
                }
                v.add(i, j, avg/vectors.size());
            }
        }
        return v;
    }

    public static double distance(vector x, vector y, int change){
        double dis = 0;
        for(int i =0; i<vectorHeight; i++){
            for (int j =0; j<vectorWidth; j++){
                dis += Math.pow(x.getElement(i, j)-y.getElement(i, j) + change, 2);
            }
        }
        return dis;
    }

    public static void addCode(ArrayList<vector> vectors){
        codes = new ArrayList<>();
        for(int i =0; i<vectors.size(); i++){
            double minDis = distance(vectors.get(i), codeBook.get(0), 0);
            int index = 0;
            for(int j =1; j<codeBook.size(); j++){
                double dis = distance(vectors.get(i), codeBook.get(j), 0);
                if(dis < minDis){
                    minDis = dis;
                    index = j;
                }
            }
            codes.add(index);
        }

    }

    public static void quantize(ArrayList<vector> vectors, int level, ArrayList<vector> all){
        if(level == 1 || vectors.size() == 0){
            if(vectors.size()>0)
                codeBook.add(getAverage(vectors));
            return;
        }

        vector vec = getAverage(vectors);
        ArrayList<vector> left = new ArrayList<>();
        ArrayList<vector> right = new ArrayList<>();
        for(int i =0; i<all.size(); i++){
            double lDis = distance(all.get(i), vec, -1);
            double rDis = distance(all.get(i), vec, 1);
            if(lDis<rDis){
                left.add(all.get(i));
            }
            else{
                right.add(all.get(i));
            }
        }

        quantize(left, level/2, all);
        quantize(right, level/2, all);
    }

    public static void compress(String path){
        int[][] pixels = ImageRW.readImage(path);
        height = ImageRW.height;
        width = ImageRW.width;
        pixels = scaleImage(height, width, pixels);
        ArrayList<vector> vectors = createImageVectors(pixels);
        quantize(vectors, codeBookSize, vectors);
        addCode(vectors);

    }

    public static void decompress(String path){
        int scaledHeight;
        int scaledWidth;
        if(height%vectorHeight == 0){
            scaledHeight = height;
        }
        else{
            scaledHeight = (height/vectorHeight+1)*vectorHeight;
        }

        if(width%vectorWidth == 0){
            scaledWidth = width;
        }
        else{
            scaledWidth = (width/vectorWidth+1)*vectorWidth;
        }
        int scaledImage[][] = new int[scaledHeight][scaledWidth];

        int idx = 0;

        for (int row = 0; row < scaledHeight; row += vectorHeight) {
            for (int column = 0; column < scaledWidth; column += vectorWidth) {
                vector vec = codeBook.get(codes.get(idx));

                for (int i = 0; i < vectorHeight; i++) {
                    for (int j = 0; j < vectorWidth; j++) {
                        scaledImage[i+row][j+column] = (int) vec.getElement(i, j);
                    }
                }
                idx++;
            }
        }

        /*for(int x = 0; x < codes.size(); x++){

            for(int i =0; i<vectorHeight; i++){
                for (int j = 0; j< vectorWidth; j++){
                    scaledImage[i+h][j+w] = (int) vec.getElement(i, j);
                }
            }
            h += vectorHeight;
            w += vectorWidth;
        }*/

        int image[][] = new int[height][width];
        for(int i =0; i<height; i++){
            for (int j =0; j<width; j++){
                image[i][j] = scaledImage[i][j];
            }
        }
        ImageRW.writeImage(image, width, height, path);
    }

    public static void main(String[] args){
        /*System.out.print("CodeBook size: ");
        Scanner sc = new Scanner(System.in);
        codeBookSize = sc.nextInt();
        System.out.print("Vector height: ");
        vectorHeight = sc.nextInt();
        System.out.print("Vector width: ");
        vectorWidth = sc.nextInt();
        compress("11.jpeg");
        decompress("decompressed.jpg");*/

        JFrame f = new JFrame("Image Vector Quantization");
        f.setContentPane(new gui().getPanel());
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        f.pack();
        f.setSize(800,400);
        f.setVisible(true);

    }
}
