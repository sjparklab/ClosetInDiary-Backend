package me.parkseongjong.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkseongjong.springbootdeveloper.domain.Article;
import me.parkseongjong.springbootdeveloper.dto.AddArticleRequest;
import me.parkseongjong.springbootdeveloper.dto.UpdateArticleRequest;
import me.parkseongjong.springbootdeveloper.repository.BlogRepository;
import org.hibernate.cache.cfg.internal.AbstractDomainDataCachingConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;

    public Article save(AddArticleRequest request) {
        return blogRepository.save(request.toEntity());
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    public void delete(Long id) {
        blogRepository.deleteById(id);
    }

    @Transactional
    public Article update(Long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        article.update(request.getTitle(), request.getContent());

        return article;
    }
}
