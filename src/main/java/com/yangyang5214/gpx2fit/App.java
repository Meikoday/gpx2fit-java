package com.yangyang5214;


import com.garmin.fit.*;

import java.util.Calendar;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        encodeActivity();
    }

    private static void encodeActivity() {
        FileEncoder encode;
        try {
            encode = new FileEncoder(new java.io.File("result.fit"), Fit.ProtocolVersion.V2_0);
        } catch (FitRuntimeException e) {
            System.err.println("Error opening file ExampleActivity.fit");
            return;
        }

        //Generate FileIdMessage
        FileIdMesg fileIdMesg = new FileIdMesg(); // Every FIT file MUST contain a 'File ID' message as the first message
        fileIdMesg.setManufacturer(Manufacturer.DEVELOPMENT);
        fileIdMesg.setType(File.ACTIVITY);
        fileIdMesg.setProduct(1);
        fileIdMesg.setSerialNumber(12345L);

        encode.write(fileIdMesg); // Encode the FileIDMesg

        byte[] appId = new byte[]{
                0x1, 0x1, 0x2, 0x3,
                0x5, 0x8, 0xD, 0x15,
                0x22, 0x37, 0x59, (byte) 0x90,
                (byte) 0xE9, 0x79, 0x62, (byte) 0xDB
        };

        DeveloperDataIdMesg developerIdMesg = new DeveloperDataIdMesg();
        for (int i = 0; i < appId.length; i++) {
            developerIdMesg.setApplicationId(i, appId[i]);
        }
        developerIdMesg.setDeveloperDataIndex((short) 0);
        encode.write(developerIdMesg);

        FieldDescriptionMesg fieldDescMesg = new FieldDescriptionMesg();
        fieldDescMesg.setDeveloperDataIndex((short) 0);
        fieldDescMesg.setFieldDefinitionNumber((short) 0);
        fieldDescMesg.setFitBaseTypeId((short) Fit.BASE_TYPE_SINT8);
        fieldDescMesg.setFieldName(0, "doughnuts_earned");
        fieldDescMesg.setUnits(0, "doughnuts");
        encode.write(fieldDescMesg);

        FieldDescriptionMesg hrFieldDescMesg = new FieldDescriptionMesg();
        hrFieldDescMesg.setDeveloperDataIndex((short) 0);
        hrFieldDescMesg.setFieldDefinitionNumber((short) 1);
        hrFieldDescMesg.setFitBaseTypeId((short) Fit.BASE_TYPE_UINT8);
        hrFieldDescMesg.setFieldName(0, "hr");
        hrFieldDescMesg.setUnits(0, "bpm");
        hrFieldDescMesg.setNativeFieldNum((short) RecordMesg.HeartRateFieldNum);
        encode.write(hrFieldDescMesg);

        RecordMesg record = new RecordMesg();
        DeveloperField doughnutsEarnedField = new DeveloperField(fieldDescMesg, developerIdMesg);
        DeveloperField hrDevField = new DeveloperField(hrFieldDescMesg, developerIdMesg);
        record.addDeveloperField(doughnutsEarnedField);
        record.addDeveloperField(hrDevField);

        record.setHeartRate((short) 140);
        hrDevField.setValue((short) 140);
        record.setCadence((short) 88);
        record.setDistance(510f);
        record.setSpeed(2800f);
        doughnutsEarnedField.setValue(1);
        encode.write(record);

        record.setHeartRate(Fit.UINT8_INVALID);
        hrDevField.setValue((short) 143);
        record.setCadence((short) 90);
        record.setDistance(2080f);
        record.setSpeed(2920f);
        doughnutsEarnedField.setValue(2);
        encode.write(record);

        record.setHeartRate((short) 144);
        hrDevField.setValue((short) 144);
        record.setCadence((short) 92);
        record.setDistance(3710f);
        record.setSpeed(3050f);
        doughnutsEarnedField.setValue(3);
        encode.write(record);

        try {
            encode.close();
        } catch (FitRuntimeException e) {
            System.err.println("Error closing encode.");
            return;
        }

        System.out.println("Encoded FIT file ExampleActivity.fit.");
    }
}