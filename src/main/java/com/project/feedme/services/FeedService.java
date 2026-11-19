package com.project.feedme.services;


import com.project.feedme.dtos.FeedItemResponse;
import com.project.feedme.dtos.FollowResponse;
import com.project.feedme.dtos.FollowingResponse;
import com.project.feedme.dtos.TimelinePageResponse;
import com.project.feedme.models.FollowRelation;
import com.project.feedme.models.Post;
import com.project.feedme.models.UserProfile;
import com.project.feedme.repository.FollowRelationRepository;
import com.project.feedme.repository.PostCreationRequestRepository;
import com.project.feedme.repository.PostRepository;
import com.project.feedme.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedService {
    private static final Comparator<FeedItemResponse> FEED_ORDER =
            Comparator.comparing(
                    FeedItemResponse::createdAt,
                    Comparator.reverseOrder()
            ).thenComparing(FeedItemResponse::postId);

    private final FeedMetricsService feedMetricsService;
    private final UserProfileRepository userProfileRepository;
    private final FollowRelationRepository followRelationRepository;
    private final FeedCacheService feedCacheService;
    private final PostRepository postRepository;
    private final PostCreationRequestRepository postCreationRequestRepository;
    private final FeedEventOutboxService feedEventOutboxService;

    public TimelinePageResponse getHomeFeed(String userId,
                                            String cursor,
                                            int limit){
        long startedAtNanos = feedMetricsService.startTimer();
        int pagesize = normalizeLimit(limit);
        feedMetricsService.recordHomeFeedRequestedPageSize(limit,pagesize);

        try {
            UserProfile viewer = getUser(userId);
            FeedCursorCodec.FeedCursor pageCursor = FeedCursorCodec.parse(cursor);
            VisibleAuthors visibleAuthors = getVisibleAuthors(viewer);

            NomralFeedSliceResult nomralFeedSliceResult =
                    getNormalHomeFeedSlice
        }
    }

    private UserProfile getUser(String userId){
        return userProfileRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
    }

    private NormalFeedSliceResult getNormalHomeFeedSlice(String userId,
                                                         Set<String> nonHotAuthorIds,
                                                         FeedCursorCodec.FeedCursor pageCursor ,
                                                         int pageSize){
        if(nonHotAuthorIds.isEmpty()){
            FeedSlice slice = new FeedSlice(List.of() , false);
            return new NormalFeedSliceResult(slice,"empty_non_hot");
        }

        Optional<FeedSlice> new FeedSlice(List.of(), false);

        if(cachedSlice.isPresent()){
            return new NormalFeedSliceResult(cachedSlice.get() , "hit");
        }

        List<Post> posts =

    }

    private VisibleAuthors getVisibleAuthors(UserProfile viewer){
        List<FollowRelation> followRelations = followRelationRepository.findByFollower_Id(viewer.getId());

        Set<String> allAuthorsIds = new LinkedHashSet<>();
        Set<String> hotAuthorIds = new LinkedHashSet<>();
        Set<String> nonHotAuthorsIds = new LinkedHashSet<>();

        allAuthorsIds.add(viewer.getId());

        if(viewer.isHotUser()){
            hotAuthorIds.add(viewer.getId());
        } else{
            hotAuthorIds.add(viewer.getId());
        }

        for ( FollowRelation relation : followRelations){
            UserProfile author = relation.getTargetUser();
            allAuthorsIds.add(author.getId());
            if(author.isHotUser()){
                hotAuthorIds.add(author.getId());
            } else{
                nonHotAuthorsIds.add(author.getId());
            }
        }

        return new VisibleAuthors(allAuthorsIds,hotAuthorIds,nonHotAuthorsIds);
    }



    private record VisibleAuthors(Set<String> allAuthorIds, Set<String> hotAuthorIds, Set<String> nonHotAuthorIds) {
    }

    private record NormalFeedSliceResult(FeedSlice slice, String cacheOutcome) {
    }


}
