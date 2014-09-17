package org.whispersystems.textsecure.push;

import java.util.List;

/**
 * Created by real123 on 9/12/14.
 */
public class ContactsInfo {
    private Long id;
    private String number;
    private String nickname;
    private Boolean gender;
    private Integer age;
    private String work;
    private Long imageattachmentid;
    private String sign;
    private List<String> friends;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public Long getImageattachmentid() {
        return imageattachmentid;
    }

    public void setImageattachmentid(Long imageattachmentid) {
        this.imageattachmentid = imageattachmentid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }


    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public ContactsInfo() {

    }

    public ContactsInfo(Long id, String number, String nickname, Boolean gender,
                        Integer age, String work, Long imageattachmentid, String sign) {
        this.id = id;
        this.number = number;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.work = work;
        this.imageattachmentid = imageattachmentid;
        this.sign = sign;
    }

    public ContactsInfo(String number, String nickname, Boolean gender,
                        Integer age, String work, Long imageattachmentid, String sign) {
        this.number = number;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.work = work;
        this.imageattachmentid = imageattachmentid;
        this.sign = sign;
    }

}
