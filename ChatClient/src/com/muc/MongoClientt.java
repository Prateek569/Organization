package com.muc;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import java.io.*;


public class MongoClientt {

    private final MongoClient mongoClient;

    public MongoClientt(MongoClientURI uri) {
        mongoClient = new MongoClient(uri);
    }

    public void DownloadFiles(String dbName, String BucketName, String name, String path){

        MongoDatabase database = mongoClient.getDatabase(dbName);
        GridFSBucket gridFSBucket = GridFSBuckets.create(database,BucketName);
        try {
            FileOutputStream streamToDownloadTo = new FileOutputStream(path);
            gridFSBucket.downloadToStream(name, streamToDownloadTo);
            streamToDownloadTo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
