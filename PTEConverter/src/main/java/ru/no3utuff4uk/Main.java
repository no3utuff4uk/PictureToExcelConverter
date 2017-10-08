package ru.no3utuff4uk;


import ru.no3utuff4uk.converter.PTEConverterImpl;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        PTEConverterImpl.Builder builder = new PTEConverterImpl.Builder();
        builder.setOutputFile(new File("keke"));
        builder.getOutputFile();
    }
}
