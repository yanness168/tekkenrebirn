package com.cpan228.tekkenrebirn.controllers;

import com.cpan228.tekkenrebirn.model.Fighter;
import com.cpan228.tekkenrebirn.model.FighterSearchedByDto;
import com.cpan228.tekkenrebirn.model.HeroPool;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


@Controller
public class DesignController {
    private final HeroPool hp;

    @Autowired
    public DesignController(HeroPool hp) {
        this.hp = hp;
    }


    @GetMapping("/add_fighter")
    public String showFighterForm(Model model) {
        model.addAttribute("fighter", new Fighter());
        return "add_fighter";
    }

    @PostMapping("/add_fighter")
    public String processFighterForm(@Valid @ModelAttribute("fighter") Fighter fighter, BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            return "add_fighter";
//        }

        // Perform additional validations
        if (fighter.getHealth() <= 1000) {
            bindingResult.rejectValue("health", "error.fighter", "Health must be more than 1000");
            return "add_fighter";
        }

        if (fighter.getDamage() >= 100) {
            bindingResult.rejectValue("damage", "error.fighter", "Damage must be less than 100");
            return "add_fighter";
        }

        if (fighter.getResistance().compareTo(BigDecimal.ZERO) < 0 || fighter.getResistance().compareTo(BigDecimal.TEN) > 0) {
            bindingResult.rejectValue("resistance", "error.fighter", "Resistance must be between 0 and 10");
            return "add_fighter";
        }


        System.out.println("Submitted fighter: " + fighter);
        // Add the fighter to the hero pool
        var id = hp.saveFighter(fighter);
        System.out.println("Saved fighter: " + id);
        return "redirect:/hero_pool";
    }

    @GetMapping("/hero_pool")
    public String showHeroPool(Model model, @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        var pageSize = 5;
        var fighterPage = hp.getFighters(PageRequest.of(page, pageSize));
        model.addAttribute("fighters", fighterPage.getContent());
        model.addAttribute("currentPage", fighterPage.getNumber());
        model.addAttribute("totalPages", fighterPage.getTotalPages());
        model.addAttribute("fightersSearchedByDto", new FighterSearchedByDto());
        return "hero_pool";
    }

    @PostMapping("/hero_pool/filtered")
    public String searchFightersByDate(@Valid @ModelAttribute("fightersSearchedByDto") FighterSearchedByDto dto,
                                       BindingResult bd, Model model,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") int page)
            throws ParseException {
        if (bd.hasErrors()) {
            return "redirect:/hero_pool";
        }

        System.out.println("Submitted info: " + dto);

        var dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        var filteredFighters = hp.getFighterByNameStartsWithAndCreatedAtBetween(
                dto.getName(),
                dateFormatter.parse(dto.getStartDate()),
                dateFormatter.parse(dto.getEndDate())
        );

        var pageSize = 5;
        var filteredFighterPage = hp.getFilteredFighterPage(filteredFighters, page, pageSize);
        model.addAttribute("fighters", filteredFighterPage.getContent());
        model.addAttribute("currentPage", filteredFighterPage.getNumber());
        model.addAttribute("totalPages", filteredFighterPage.getTotalPages());
        model.addAttribute("fightersSearchedByDto", new FighterSearchedByDto());
        return "hero_pool";
    }

    @GetMapping("/hero_pool/switchPage")
    public String switchPage(@RequestParam("pageToSwitch") int pageToSwitch, Model model) {
        var pageSize = 5;
        var fighterPage = hp.getFighters(PageRequest.of(pageToSwitch, pageSize));
        List<Fighter> paginatedFighters = hp.getPaginatedFighters(pageToSwitch);
        model.addAttribute("fighters", paginatedFighters);
        model.addAttribute("currentPage", pageToSwitch);
        model.addAttribute("totalPages", fighterPage.getTotalPages());
        model.addAttribute("fightersSearchedByDto", new FighterSearchedByDto());
        return "hero_pool";
    }
}
