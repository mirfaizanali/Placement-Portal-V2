package com.placement.portal.service.placement;

import com.placement.portal.domain.TrainingModule;
import com.placement.portal.dto.request.TrainingModuleRequest;
import com.placement.portal.dto.response.TrainingModuleDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.TrainingModuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingModuleRepository repository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<TrainingModuleDto> listAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(entityMapper::toTrainingModuleDto)
                .toList();
    }

    @Transactional
    public TrainingModuleDto create(TrainingModuleRequest req) {
        TrainingModule saved = repository.save(fromRequest(new TrainingModule(), req));
        return entityMapper.toTrainingModuleDto(saved);
    }

    @Transactional
    public TrainingModuleDto update(String id, TrainingModuleRequest req) {
        TrainingModule existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TrainingModule", id));
        fromRequest(existing, req);
        return entityMapper.toTrainingModuleDto(repository.save(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("TrainingModule", id);
        }
        repository.deleteById(id);
    }

    @PostConstruct
    @Transactional
    public void seedIfEmpty() {
        if (repository.count() > 0) {
            return;
        }
        log.info("Seeding default training modules");
        repository.saveAll(List.of(
                module(1, "HTML & CSS Basics", "code",
                        "https://www.w3schools.com/html/",
                        "Learn the structural and styling foundations of every web page. " +
                        "You will build pages with semantic HTML and style them with modern CSS, " +
                        "covering selectors, the box model, flexbox and responsive layouts. " +
                        "A must-have skill for any frontend or full-stack interview."),
                module(2, "JavaScript Basics", "javascript",
                        "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide",
                        "Master the language that powers interactivity on the web. " +
                        "MDN's official guide walks through variables, control flow, functions, " +
                        "objects, the DOM and async patterns. " +
                        "Strong JavaScript fundamentals open doors to web, mobile and Node.js roles."),
                module(3, "Java Fundamentals", "coffee",
                        "https://www.w3schools.com/java/",
                        "Get hands-on with one of the most in-demand languages for campus placements. " +
                        "Covers syntax, OOP, collections, exception handling and basic I/O. " +
                        "Java fluency is expected at most service-based and product companies hiring freshers."),
                module(4, "Python Basics", "terminal",
                        "https://www.w3schools.com/python/",
                        "Pick up Python — the go-to language for scripting, data, and automation. " +
                        "Learn data types, control flow, functions, file handling, and standard libraries. " +
                        "Python is the easiest second language to add to your resume and unlocks data-science roles."),
                module(5, "Communication Skills / Interview Preparation", "record_voice_over",
                        "https://www.w3schools.com/howto/howto_interview.asp",
                        "Technical skill alone doesn't land offers — clear communication does. " +
                        "Walk through common HR and behavioural questions, structure your answers using STAR, " +
                        "and learn how to talk about projects with impact. " +
                        "Pair this with the technical modules for a complete placement-ready toolkit.")
        ));
    }

    private TrainingModule fromRequest(TrainingModule target, TrainingModuleRequest req) {
        target.setTitle(req.getTitle());
        target.setDescription(req.getDescription());
        target.setLearningLink(req.getLearningLink());
        target.setIconName(req.getIconName());
        target.setDisplayOrder(req.getDisplayOrder());
        return target;
    }

    private TrainingModule module(int order, String title, String icon, String link, String description) {
        return TrainingModule.builder()
                .displayOrder(order)
                .title(title)
                .iconName(icon)
                .learningLink(link)
                .description(description)
                .build();
    }
}
