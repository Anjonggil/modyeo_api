package com.co.kr.modyeo.member.service.impl;

import com.co.kr.modyeo.common.exception.ApiException;
import com.co.kr.modyeo.common.exception.code.TeamErrorCode;
import com.co.kr.modyeo.common.exception.code.MemberErrorCode;
import com.co.kr.modyeo.member.domain.entity.Team;
import com.co.kr.modyeo.member.domain.entity.Member;
import com.co.kr.modyeo.member.domain.entity.link.Crew;
import com.co.kr.modyeo.member.domain.entity.link.MemberTeam;
import com.co.kr.modyeo.member.domain.enumerate.JoinStatus;
import com.co.kr.modyeo.member.repository.CrewRepository;
import com.co.kr.modyeo.member.repository.TeamRepository;
import com.co.kr.modyeo.member.repository.MemberTeamRepository;
import com.co.kr.modyeo.member.repository.MemberRepository;
import com.co.kr.modyeo.member.service.TeamApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamApplicationServiceImpl implements TeamApplicationService {
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final CrewRepository crewRepository;

    @Override
    public MemberTeam applicantCrew(Long memberId, Long crewId) {
        Member member = findMember(memberId);
        Team team = findTeam(crewId);
        MemberTeam memberTeam = memberTeamRepository.findByTeamAndMember(team,member);
        if (memberTeam != null){
            if (memberTeam.getJoinStatus() != JoinStatus.APPROVAL){
                throw ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorCode(TeamErrorCode.ALREADY_JOINED_TEAM.getCode())
                        .errorMessage(TeamErrorCode.ALREADY_JOINED_TEAM.getMessage())
                        .build();
            }
        }


        MemberTeam newMemberTeam = MemberTeam.joinApplicationBuilder()
                .team(team)
                .member(member)
                .build();

        return memberTeamRepository.save(newMemberTeam);
    }

    @Override
    public MemberTeam updateJoinStatus(Long memberCrewId, JoinStatus joinStatus) {
        MemberTeam memberTeam = memberTeamRepository.findMemberTeamById(memberCrewId)
                .orElseThrow(() -> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorCode(TeamErrorCode.NOT_FOUND_APPLICANT.getCode())
                        .errorMessage(TeamErrorCode.NOT_FOUND_APPLICANT.getMessage())
                        .build());

        if (JoinStatus.DENIAL.equals(joinStatus)){
            memberTeam.chanegeDenial();
        }else{
            Crew crew = Crew.createCrewBuilder()
                    .member(memberTeam.getMember())
                    .team(memberTeam.getTeam())
                    .build();

            crewRepository.save(crew);
            memberTeamRepository.delete(memberTeam);
        }

        return memberTeam;
    }

    private Member findMember(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorMessage(MemberErrorCode.NOT_FOUND_MEMBER.getMessage())
                        .errorCode(MemberErrorCode.NOT_FOUND_MEMBER.getCode())
                        .build());
    }

    private Team findTeam(Long teamId){
        return teamRepository.findById(teamId)
                .orElseThrow(() -> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorMessage(TeamErrorCode.NOT_FOUND_TEAM.getMessage())
                        .errorCode(TeamErrorCode.NOT_FOUND_TEAM.getCode())
                        .build());
    }

}
