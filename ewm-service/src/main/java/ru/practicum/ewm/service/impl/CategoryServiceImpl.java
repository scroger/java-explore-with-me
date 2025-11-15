package ru.practicum.ewm.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.QEvent;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.service.CategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Category name already exists");
        }

        Category cat = CategoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(cat);

        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category cat = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!cat.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Category name already exists");
        }

        cat.setName(dto.name());
        Category saved = categoryRepository.save(cat);

        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category cat = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        QEvent e = QEvent.event;
        long count = new JPAQueryFactory(entityManager)
                .selectFrom(e)
                .where(e.category.id.eq(catId))
                .fetchCount();
        if (count > 0) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(cat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pg = PageRequest.of(from / size, size);
        List<Category> list = categoryRepository.findAll(pg).getContent();

        return list.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long catId) {
        Category cat = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return CategoryMapper.toDto(cat);
    }


}
