package com.gym.fitconnectpro.fragments.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.activities.member.FriendRequestsAdapter;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.dao.SocialDAO;
import com.gym.fitconnectpro.services.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendRequestsFragment extends Fragment { // Fixed Package name to match folder structure if needed, but using existing folder pattern

    private RecyclerView rvReceived, rvSent;
    private SocialDAO socialDAO;
    private int currentMemberId = -1;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        
        rvReceived = view.findViewById(R.id.rvReceivedRequests);
        rvSent = view.findViewById(R.id.rvSentRequests);
        
        socialDAO = new SocialDAO(requireContext());
        identifyCurrentMember();
        
        rvReceived.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSent.setLayoutManager(new LinearLayoutManager(getContext()));
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }
    
    private void identifyCurrentMember() {
        try {
            int userId = Session.getInstance(requireContext()).getUserId();
            MemberDashboardDAO dashboardDAO = new MemberDashboardDAO(requireContext());
            Map<String, String> info = dashboardDAO.getMemberHeaderInfo(userId);
            if (info != null && info.containsKey("member_id")) {
                currentMemberId = Integer.parseInt(info.get("member_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadRequests() {
        if (currentMemberId == -1) return;
        
        // Load Received
        List<SocialDAO.FriendRequest> received = socialDAO.getPendingRequests(currentMemberId, false);
        FriendRequestsAdapter receivedAdapter = new FriendRequestsAdapter(requireContext(), received, this::loadRequests);
        rvReceived.setAdapter(receivedAdapter);
        
        // Load Sent
        List<SocialDAO.FriendRequest> sent = socialDAO.getPendingRequests(currentMemberId, true);
        FriendRequestsAdapter sentAdapter = new FriendRequestsAdapter(requireContext(), sent, this::loadRequests);
        rvSent.setAdapter(sentAdapter);
    }
}
