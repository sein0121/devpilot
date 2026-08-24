package com.devpilot.dto.response;

import com.devpilot.domain.RoadmapLink;

public record RoadmapLinkResponse(
        Long id,
        String url,
        String label
) {
    public static RoadmapLinkResponse from(RoadmapLink link) {
        return new RoadmapLinkResponse(link.getId(), link.getUrl(), link.getLabel());
    }
}