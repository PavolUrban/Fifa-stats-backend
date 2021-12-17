package com.javasampleapproach.springrest.mysql.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Entity
@Data
@Table(name = "file_model")
public class FileModel {
    @Id
    @GeneratedValue
    @Column(name = "id")
    @JsonView(View.FileInfo.class)
    private Long id;

    @Column(name = "name")
    @JsonView(View.FileInfo.class)
    private String name;

    @Column(name = "mimetype")
    private String mimetype;

    @Lob
    @Column(name = "pic")
    private byte[] pic;

    @Column(name = "teamname")
    private String teamname;


    public FileModel() {
    }

    public FileModel(String name, String mimetype, byte[] pic, String teamName) {
        this.name = name;
        this.mimetype = mimetype;
        this.pic = pic;
        this.teamname = teamName;
    }

}