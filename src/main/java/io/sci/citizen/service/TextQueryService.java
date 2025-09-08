package io.sci.citizen.service;

import io.sci.citizen.model.QueryOption;
import io.sci.citizen.model.Section;
import io.sci.citizen.model.TextQuery;
import io.sci.citizen.model.dto.QueryOptionRequest;
import io.sci.citizen.model.dto.TextQueryRequest;
import io.sci.citizen.model.repository.QueryOptionRepository;
import io.sci.citizen.model.repository.SectionRepository;
import io.sci.citizen.model.repository.TextQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TextQueryService extends BaseService {

    private final TextQueryRepository qRepo;
    private final QueryOptionRepository oRepo;
    private final SectionRepository sectionRepo;

    public TextQueryService(TextQueryRepository qRepo,
                            QueryOptionRepository oRepo,
                            SectionRepository sectionRepo) {
        this.qRepo = qRepo; this.oRepo = oRepo; this.sectionRepo = sectionRepo;
    }

    @Transactional(readOnly = true)
    public List<TextQuery> list(Long sectionId) {
        if (sectionId==null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Optional<Section> opt = sectionRepo.findById(sectionId);
        if (opt.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!isAdmin()) {
            if (!Objects.equals(getUser().getId(), opt.get().getProject().getCreator().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return qRepo.findBySection_IdOrderBySequenceAsc(sectionId);
    }

    @Transactional(readOnly = true)
    public TextQuery getById(Long id) {
        return qRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public TextQuery create(TextQueryRequest req) {
        TextQuery t = new TextQuery();
        apply(t, req);
        return qRepo.save(t);
    }

    @Transactional
    public TextQuery update(Long id, TextQueryRequest req) {
        TextQuery t = getById(id);
        apply(t, req);
        return qRepo.save(t);
    }


    private void apply(TextQuery t, TextQueryRequest req) {
        t.setAttribute(req.getAttribute());
        t.setQuestion(req.getQuestion());
        t.setType(req.getType());
        t.setEnabled(req.isEnabled());
        t.setSequence(req.getSequence());
        t.setRequired(req.isRequired());

        if (req.getSectionId() != null) {
            Section p = sectionRepo.findById(req.getSectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section not found"));
            t.setSection(p);
        } else {
            t.setSection(null);
        }
    }

    @Transactional
    public TextQuery save(TextQueryRequest req) {
        TextQuery q = (req.getId() == null) ? new TextQuery() : getById(req.getId());
        q.setAttribute(req.getAttribute());
        q.setQuestion(req.getQuestion());
        q.setType(req.getType());
        q.setEnabled(req.isEnabled());
        q.setSequence(req.getSequence());
        q.setRequired(req.isRequired());

        if (req.getSectionId() != null)
            q.setSection(sectionRepo.findById(req.getSectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Section not found")));
        else
            q.setSection(null);

        TextQuery textQuery = qRepo.save(q);
        if (req.getType()==1||req.getType()==2||req.getType()==4||req.getType()==6){
            for (QueryOptionRequest optionRequest : req.getOptions()) {
                QueryOption p = new QueryOption();
                if (optionRequest.getSequence() != null) {
                    if (optionRequest.getId() != null) {
                        Optional<QueryOption> optional = oRepo.findById(optionRequest.getId());
                        if (optional.isPresent()) {
                            p = optional.get();
                        }
                    }
                    p.setSequence(optionRequest.getSequence());
                    p.setDescription(optionRequest.getDescription());
                    p.setQuestion(textQuery);
                    p.setEnabled(optionRequest.isEnabled());
                    oRepo.save(p);
                }
            }
        }
        /**q.setOptions(new ArrayList<>());
        req.getOptions().stream()
                .filter(p -> p.getDescription() != null && !p.getDescription().isBlank())
                .forEach(pr -> {
                    QueryOption p = new QueryOption();
                    p.setSequence(pr.getSequence());
                    p.setDescription(pr.getDescription());
                    p.setQuestion(q);
                    q.getOptions().add(p);
                });

        return qRepo.save(q);*/
        return textQuery;
    }

    @Transactional
    public void delete(Long id) { qRepo.deleteById(id); }
}