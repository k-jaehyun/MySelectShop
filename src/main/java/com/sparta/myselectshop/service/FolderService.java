package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.FolderRequestDto;
import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepositoty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepositoty folderRepositoty;
    public void addFolders(List<String> folderNames, User user) {

        // 여러개를 한번에 조건으로 주려면 뒤에 In을 붙이면 됨. And는 where로 볼 수 있음. 회원, 그리고 폴더테이블의 여러개(in)의 이름으로 모든 폴더 조회
        // select * from folder where user_id = ? and name in (?, ? ,?);
        List<Folder> existFolderList = folderRepositoty.findAllByUserAndNameIn(user,folderNames);

        List<Folder> folderList = new ArrayList<>();

        // 이미 존재하던 폴더 이름인지 확인하고 추가
        for (String folderName : folderNames) {
            if (!isExistFolderName(folderName, existFolderList)) {
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            } else {
                throw new IllegalArgumentException("중볻된 폴더명을 제거해주세요! 폴더명: " + folderName);
            }
        }

        folderRepositoty.saveAll(folderList);
    }

    public List<FolderResponseDto> getFolders(User user) {
        List<Folder> folderList = folderRepositoty.findByUser(user);
        List<FolderResponseDto> responseDtoList = new ArrayList<>();

        for (Folder folder : folderList) {
            responseDtoList.add(new FolderResponseDto(folder));
        }

        return responseDtoList;
    }

    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
        for (Folder existFolder : existFolderList) {
            if(folderName.equals(existFolder.getName())) {
                return true;
            }
        }

        return false;
    }


}
