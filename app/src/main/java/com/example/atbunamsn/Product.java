package com.example.atbunamsn;

/**
 * Created by sherif146 on 06/04/2017.
 */
public class Product {

        private String fullname;
        private String pics_path;
        private String level;
        private String dept;
        private String email;
        private String gender;
        private String userID;
        private byte[] blobpics;

        public byte[] getBLOB(){
            return blobpics;
        }
        public void setBLOB(byte[] blobpics){
            this.blobpics = blobpics;
        }

    public String getName(){
        return fullname;
    }
    public void setName(String fullname){
        this.fullname = fullname;
    }

        public String getuserID(){
            return userID;
        }
        public void setuserID(String userID){
            this.userID = userID;
        }

        public String getPics_path(){
            return pics_path;
        }
        public void setPics_path(String pics_path){
            this.pics_path = pics_path;
        }

        public String getLevel(){
            return level;
        }
        public void setLevel(String level){
            this.level = level;
        }

        public String getDept (){
            return dept;
        }
        public void setDept (String dept ){
            this.dept  = dept;
        }

        public String getEmail (){
            return email;
        }
        public void setEmail (String email ){
            this.email  = email;
        }

        public String getGender (){
            return gender;
        }
        public void setGender (String body_two_2 ){
        this.gender  = gender;
    }

    }
