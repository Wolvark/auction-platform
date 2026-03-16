package com.auction.platform.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_media_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMediaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
