package org.habitsapp.client.session;

import org.habitsapp.server.repository.Repository;

import java.util.LinkedList;
import java.util.List;

public class AdminRequest {

    public List<String> getProfilesList(String email, String token) {
        return new LinkedList<>();
    }

    public boolean manageUserProfile(String email, String token, String emailToRemove, Repository.ProfileAction action) {
        return false;
    }

}
