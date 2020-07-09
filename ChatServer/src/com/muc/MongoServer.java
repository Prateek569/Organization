package com.muc;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import java.io.*;


public class MongoServer {

    private final MongoClient mongoClient;

    public MongoServer(MongoClientURI uri) {
        mongoClient = new MongoClient(uri);
    }

    public boolean name_of_files(String dbName , String BucketName, String Filename)
    {
        final boolean[] flag;
        flag = new boolean[]{false};
        try  {

            MongoDatabase database = mongoClient.getDatabase(dbName);
            GridFSBucket gridFSBucket = GridFSBuckets.create(database,BucketName);
            gridFSBucket.find().forEach(new Block<GridFSFile>() {
                @Override
                public void apply(GridFSFile gridFSFile) {
                    System.out.println(gridFSFile.getFilename());
                    if(Filename.equalsIgnoreCase(gridFSFile.getFilename()))
                    {
                        flag[0] = true;
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag[0];
    }

    public void UploadFiles(String dbName, String BucketName, String name, String path){

        MongoDatabase database = mongoClient.getDatabase(dbName);

        GridFSBucket gridFSBucket = GridFSBuckets.create(database,BucketName);

        try {
            InputStream istoupload = new FileInputStream(new File(path));
            ObjectId fieldId = gridFSBucket.uploadFromStream(name, istoupload);
            System.out.println(fieldId);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
