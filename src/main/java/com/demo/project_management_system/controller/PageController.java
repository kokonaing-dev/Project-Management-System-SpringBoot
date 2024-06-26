package com.demo.project_management_system.controller;

import com.demo.project_management_system.dto.IssueDto;
import com.demo.project_management_system.entity.*;
import com.demo.project_management_system.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private IssueTypeService issueTypeService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/dashboard")
    public String gettingStart(@AuthenticationPrincipal UserDetails userDetails, Model model, HttpSession session) {
        User user = userService.findUserByEmail(userDetails.getUsername());
        // Store the user object in the session
        session.setAttribute("loggedInUser", user);

        // Expose loggedInUser as a model attribute
        model.addAttribute("loggedInUser", user);

        model.addAttribute("issue", new Issue());
        model.addAttribute("project", new Project());

        List<User> users = userService.getAllActiveUsers();
        model.addAttribute("usersList", users);

        model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
        model.addAttribute("priorities", Priority.values()); // Add priorities enum

        model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
        model.addAttribute("categories", categoryService.getAllCategories());

        Set<Project> projectList = projectService.getAllProjects();
        model.addAttribute("projectList", projectList);

        // Calculate project status for each project
        for(Project project: projectList){
            int totalIssues = project.getIssues().size();
            project.setNumberOfIssues(totalIssues);
            String projectStatus = project.calculateProjectStatus();
            project.setStatus(projectStatus);
        }


        // Get the Authentication object from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Retrieve authorities from the Authentication object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if the logged-in user has ROLE_SYSTEM_ADMIN authority
        boolean isSystemAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SYSTEM_ADMIN"));

        boolean isProjectManager = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER"));

        // Check if the logged-in user has ROLE_PROJECT_MANAGER or ROLE_MEMBER authority
        boolean isProjectManagerOrMember = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER") || auth.getAuthority().equals("ROLE_MEMBER"));

        List<Issue> issues;
        if (isSystemAdmin){
            // Convert the Set to a List for the admin user
            issues = new ArrayList<>(issueService.getAllIssues());
        } else if (isProjectManagerOrMember) {
            // Fetch issues by userId for non-admin users
            issues = issueService.getIssuesByUserId(user.getId());
        }else {
            // Default to an empty list of issues for other users
            issues = new ArrayList<>();
        }
        model.addAttribute("issues", issues);


        if (isSystemAdmin) {
            // Get all projects
            List<Project> projects = projectService.getAllProjectsWithCounts();
            model.addAttribute("projects", projects);
        } else if (isProjectManagerOrMember) {
            // Get projects by user ID
            Set<Project> projects = projectService.getProjectsByUserId(user.getId());
            model.addAttribute("projects", projects);
        }
        if(isSystemAdmin){
            // Get users with authorities POLE_PROJECT_MANGER and role member
            List<User> projectManager = userService.getUsersByAuthority("ROLE_PROJECT_MANAGER");
            Set<Project> projects = projectService.getAllProjects();

            session.setAttribute("projectManager", projectManager);
            session.setAttribute("projects", projects);

            model.addAttribute("projectManager", projectManager);
            model.addAttribute("projects", projects);
            return "dashboard";
        }
        if (isProjectManager) {
            // Get users with authority ROLE_MEMBER
            List<User> members = userService.getUsersByAuthority("ROLE_MEMBER");
            Set<Project> projects = projectService.getProjectsByUserId(user.getId());

            session.setAttribute("members", members);
            session.setAttribute("projects", projects);

            model.addAttribute("members", members);
            model.addAttribute("projects", projects);
        }

        return "dashboard";
    }

    @GetMapping("/calendar")
    public String viewDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model, HttpSession session) {
        User user = userService.findUserByEmail(userDetails.getUsername());
        // Store the user object in the session
        session.setAttribute("loggedInUser", user);

        // Expose loggedInUser as a model attribute
        model.addAttribute("loggedInUser", user);

        return "calendar";
    }


    @GetMapping("/project")
    public String viewProject(@AuthenticationPrincipal UserDetails userDetails, Model model, HttpSession session){
        User user = userService.findUserByEmail(userDetails.getUsername());
        // Store the user object in the session
        session.setAttribute("loggedInUser", user);

        // Expose loggedInUser as a model attribute
        model.addAttribute("loggedInUser", user);

        model.addAttribute("issue", new Issue());
        model.addAttribute("project", new Project());

        List<User> users = userService.getAllActiveUsers();
        model.addAttribute("usersList", users);

        model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
        model.addAttribute("priorities", Priority.values()); // Add priorities enum

        model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
        model.addAttribute("categories", categoryService.getAllCategories());

//        Set<Project> projectList = projectService.getAllProjects();
//        model.addAttribute("projectList", projectList);


        // Get the Authentication object from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Retrieve authorities from the Authentication object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if the logged-in user has ROLE_SYSTEM_ADMIN authority
        boolean isSystemAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SYSTEM_ADMIN"));

        boolean isProjectManager = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER"));

        // Check if the logged-in user has ROLE_PROJECT_MANAGER or ROLE_MEMBER authority
//        boolean isProjectManagerOrMember = authorities.stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER") || auth.getAuthority().equals("ROLE_MEMBER"));



        if(isSystemAdmin){
            // Get users with authorities POLE_PROJECT_MANGER and role member
            List<User> projectManager = userService.getUsersByAuthority("ROLE_PROJECT_MANAGER");
            Set<Project> projects = projectService.getAllProjects();

            session.setAttribute("projectManager", projectManager);
            session.setAttribute("projects", projects);

            model.addAttribute("projectManager", projectManager);
            model.addAttribute("projects", projects);
            return "project-list";
        }
        if (isProjectManager) {
            // Get users with authority ROLE_MEMBER
            List<User> members = userService.getUsersByAuthority("ROLE_MEMBER");
            Set<Project> projects = projectService.getProjectsByUserId(user.getId());

            session.setAttribute("members", members);
            session.setAttribute("projects", projects);

            model.addAttribute("members", members);
            model.addAttribute("projects", projects);


        }

        return "project-list";
    }

    @GetMapping("/api/events")
    public ResponseEntity<List<IssueDto>> fetchEvents(HttpSession session, Model model) {
        // Retrieve the user object from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Pass the user object to the view
        model.addAttribute("loggedInUser", loggedInUser);

        // Get the Authentication object from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Retrieve authorities from the Authentication object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if the logged-in user has ROLE_SYSTEM_ADMIN authority
        boolean isSystemAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SYSTEM_ADMIN"));

        boolean isProjectManager = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER"));


        if (isSystemAdmin) {

            Set<Project> projectList = projectService.getProjectsByUserId(loggedInUser.getId());
            model.addAttribute("projectList", projectList);

            Set<Issue> issueList = new HashSet<>();
            for (Project project : projectList) {
                issueList.addAll(issueService.findIssueByProjectId(project.getId()));
            }
            System.out.println("ADMIN PROJECT ISSUE LIST "+ issueList);

            List<IssueDto> events = new ArrayList<>();


            for (Issue issue : issueList) {
                IssueDto eventDto = new IssueDto();
                eventDto.setId(issue.getId());
                eventDto.setIssueName(issue.getIssueType().getIssueName());
                eventDto.setPriority(issue.getPriority());
                eventDto.setProjectName(issue.getProject().getProjectName());
                eventDto.setSubject(issue.getSubject());
                eventDto.setPlanStartDate(issue.getPlanStartDate());
                eventDto.setPlanDueDate(issue.getPlanDueDate());

                // Add event to the list
                events.add(eventDto);
            }
            return ResponseEntity.ok().body(events);

        }


        if (isProjectManager) {

            Set<Project> projectList = projectService.getProjectsByUserId(loggedInUser.getId());
            model.addAttribute("projectList", projectList);

            Set<Issue> issueList = new HashSet<>();
            for (Project project : projectList) {
                issueList.addAll(issueService.findIssueByProjectId(project.getId()));
            }

            List<IssueDto> events = new ArrayList<>();


            for (Issue issue : issueList) {
                IssueDto eventDto = new IssueDto();
                eventDto.setId(issue.getId());
                eventDto.setIssueName(issue.getIssueType().getIssueName());
                eventDto.setPriority(issue.getPriority());
                eventDto.setProjectName(issue.getProject().getProjectName());
                eventDto.setSubject(issue.getSubject());
                eventDto.setPlanStartDate(issue.getPlanStartDate());
                eventDto.setPlanDueDate(issue.getPlanDueDate());

                // Add event to the list
                events.add(eventDto);
            }
            return ResponseEntity.ok().body(events);

        }


        List<Issue> issueList = issueService.getIssuesByUserId(loggedInUser.getId());

        List<IssueDto> events = new ArrayList<>();


        for (Issue issue : issueList) {
            IssueDto eventDto = new IssueDto();
            eventDto.setId(issue.getId());
            eventDto.setIssueName(issue.getIssueType().getIssueName());
            eventDto.setPriority(issue.getPriority());
            eventDto.setProjectName(issue.getProject().getProjectName());
            eventDto.setSubject(issue.getSubject());
            eventDto.setPlanStartDate(issue.getPlanStartDate());
            eventDto.setPlanDueDate(issue.getPlanDueDate());

            // Add event to the list
            events.add(eventDto);
        }

        return ResponseEntity.ok().body(events);
    }



    @GetMapping("/")
    public String authSignIn(Model model){
        return "auth-login";
    }


    @GetMapping("/register")
    public String authSignUp(ModelMap modelMap){
        modelMap.addAttribute("user", new User());
        return "auth-register";
    }

    @GetMapping("/pages-profile")
    public String pagesProfile(HttpSession session, Model model){
        // Retrieve the user object from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        long userId = loggedInUser.getId();
        System.out.println("User IDDDDDD " + userId);

        // Find projects for the logged-in user
        Set<Project> userProjects = userService.findActiveProjectsByUserId(userId);
        System.out.println("User Projects " + userProjects);

        // Fetch the number of issues for each project
        for (Project project : userProjects) {
            int numberOfIssues = projectService.findNumberOfIssuesByProjectId(project.getId());
            System.out.println("Number Of Issues.........is " + numberOfIssues);
            model.addAttribute("numberOfIssues", numberOfIssues);
            project.setNumberOfIssues(numberOfIssues);
            String projectStatus = project.calculateProjectStatus();
            project.setStatus(projectStatus);
        }

        model.addAttribute("projectList", userProjects);


        // Pass the user object to the view
        model.addAttribute("loggedInUser", loggedInUser);

        System.out.println("Logged In User ID " + loggedInUser.getId());

        return "pages-profile";
    }

    @GetMapping("/auth-logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:";
    }

    @GetMapping("/admins/role")
    public String rolePage(){
        return "users-role-list";
    }

    @GetMapping("/board")
    public String viewBoards(Model model,HttpSession session){
        // Retrieve the user object from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Pass the user object to the view
        model.addAttribute("loggedInUser", loggedInUser);


        // Get the Authentication object from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Retrieve authorities from the Authentication object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if the logged-in user has ROLE_SYSTEM_ADMIN authority
        boolean isSystemAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        // Add authorities as model attributes
        model.addAttribute("isSystemAdmin", isSystemAdmin);
        System.out.println("ADMIN AUTHORITY: " + isSystemAdmin);

        boolean isProjectManager = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER"));


        if (isSystemAdmin){
            Set<Issue> issueList = issueService.getAllIssues();

            // Filter issues by status
            List<Issue> todoIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.OPEN && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> inProgressIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.IN_PROGRESS && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> solvedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.SOLVED && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> pendingIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.PENDING && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> closedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.CLOSED && issue.getStatus() == 1)
                    .collect(Collectors.toList());


            // Add filtered lists to the model
            model.addAttribute("todoIssues", todoIssues);
            model.addAttribute("inProgressIssues", inProgressIssues);
            model.addAttribute("solvedIssues", solvedIssues);
            model.addAttribute("pendingIssues", pendingIssues);
            model.addAttribute("closedIssues", closedIssues);


            model.addAttribute("issue", new Issue());
            model.addAttribute("project", new Project());

            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
            model.addAttribute("priorities", Priority.values()); // Add priorities enum
            model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
            model.addAttribute("categories", categoryService.getAllCategories());

            return "apps-kanban";

        }



        if (isProjectManager) {

            Set<Project> projectList = projectService.getProjectsByUserId(loggedInUser.getId());
            model.addAttribute("projectList", projectList);


            Set<Issue> issueList = new HashSet<>();
            for (Project project : projectList) {
                issueList.addAll(issueService.findIssueByProjectId(project.getId()));
            }


            // Filter issues by status
            List<Issue> todoIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.OPEN && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> inProgressIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.IN_PROGRESS && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> solvedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.SOLVED && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> pendingIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.PENDING && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> closedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.CLOSED && issue.getStatus() == 1)
                    .collect(Collectors.toList());


            // Add filtered lists to the model
            model.addAttribute("todoIssues", todoIssues);
            model.addAttribute("inProgressIssues", inProgressIssues);
            model.addAttribute("solvedIssues", solvedIssues);
            model.addAttribute("pendingIssues", pendingIssues);
            model.addAttribute("closedIssues", closedIssues);


            model.addAttribute("issue", new Issue());
            model.addAttribute("project", new Project());

            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
            model.addAttribute("priorities", Priority.values()); // Add priorities enum
            model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
            model.addAttribute("categories", categoryService.getAllCategories());

            return "apps-kanban";

        }


//        Set<Issue> issues = loggedInUser.getIssues();
//        System.out.println("loggedInUser Issues"+issues);


        Set<Project> projectList = projectService.getProjectsByUserId(loggedInUser.getId());
        model.addAttribute("projectList", projectList);

        List<Issue> issueList = issueService.getIssuesByUserId(loggedInUser.getId());
//        model.addAttribute("projectList", projectList);


//        Set<Issue> issueList = new HashSet<>();
//        for (Project project : projectList) {
//            issueList.addAll(issueService.findIssueByProjectId(project.getId()));
//        }


//        Set<User> userList = new HashSet<>();
//        for (Issue issue : issues) {
//            userList.addAll(issue.getUsers());
//        }
//        System.out.println("USER LIST ...." + userList);


        // Filter issues by status
        List<Issue> todoIssues = issueList.stream()
                .filter(issue -> issue.getIssueStatus() == IssueStatus.OPEN && issue.getStatus() == 1)
                .collect(Collectors.toList());

        List<Issue> inProgressIssues = issueList.stream()
                .filter(issue -> issue.getIssueStatus() == IssueStatus.IN_PROGRESS && issue.getStatus() == 1)
                .collect(Collectors.toList());

        List<Issue> solvedIssues = issueList.stream()
                .filter(issue -> issue.getIssueStatus() == IssueStatus.SOLVED && issue.getStatus() == 1)
                .collect(Collectors.toList());

        List<Issue> pendingIssues = issueList.stream()
                .filter(issue -> issue.getIssueStatus() == IssueStatus.PENDING && issue.getStatus() == 1)
                .collect(Collectors.toList());

        List<Issue> closedIssues = issueList.stream()
                .filter(issue -> issue.getIssueStatus() == IssueStatus.CLOSED && issue.getStatus() == 1)
                .collect(Collectors.toList());


        // Add filtered lists to the model
        model.addAttribute("todoIssues", todoIssues);
        model.addAttribute("inProgressIssues", inProgressIssues);
        model.addAttribute("solvedIssues", solvedIssues);
        model.addAttribute("pendingIssues", pendingIssues);
        model.addAttribute("closedIssues", closedIssues);


        model.addAttribute("issue", new Issue());
        model.addAttribute("project", new Project());

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
        model.addAttribute("priorities", Priority.values()); // Add priorities enum
        model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "apps-kanban";
    }

    @GetMapping("/auth-recoverpw")
    public String recoverPw(){ return "auth-recoverpw"; }


    @GetMapping("/project-create")
    public String projectCreate(){
        return "project-create";
    }


    @GetMapping("/tasks-detail")
    public String viewTasksDetail(){
        return "apps-tasks-details";
    }


    @GetMapping("/apps-chat")
    public String chatTest(HttpSession session, Model model){
        // Retrieve the user object from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Pass the user object to the view
        model.addAttribute("loggedInUser", loggedInUser);
        return "apps-chat";
    }

    @GetMapping("/projects/{projectId}")
    public String getProjectDetails(@PathVariable Long projectId, Model model, HttpSession session) {
        // Retrieve the user object from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Pass the user object to the view
        model.addAttribute("loggedInUser", loggedInUser);

        Set<Project> projectList = projectService.getProjectsByUserId(loggedInUser.getId());
        model.addAttribute("projectList", projectList);

        System.out.println("WOOOOOOOOOOOOO "+projectId);
        // Assuming you have a service to fetch project details
        Project project = projectService.getProjectById(projectId);

        if (project != null) {
            Set<Issue> issueList = issueService.getIssuesByProjectId(project.getId());
            System.out.println("ISSSSSSSSSSSSLIST " + issueList);

            // Filter issues by status
            List<Issue> todoIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.OPEN && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> inProgressIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.IN_PROGRESS && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> solvedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.SOLVED && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> pendingIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.PENDING && issue.getStatus() == 1)
                    .collect(Collectors.toList());

            List<Issue> closedIssues = issueList.stream()
                    .filter(issue -> issue.getIssueStatus() == IssueStatus.CLOSED && issue.getStatus() == 1)
                    .collect(Collectors.toList());


            // Add filtered lists to the model
            model.addAttribute("todoIssues", todoIssues);
            model.addAttribute("inProgressIssues", inProgressIssues);
            model.addAttribute("solvedIssues", solvedIssues);
            model.addAttribute("pendingIssues", pendingIssues);
            model.addAttribute("closedIssues", closedIssues);


            model.addAttribute("issue", new Issue());
            model.addAttribute("project", new Project());

            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("issueStatuses", IssueStatus.values()); // Add statuses enum
            model.addAttribute("priorities", Priority.values()); // Add priorities enum
            model.addAttribute("issueTypes", issueTypeService.getAllIssueTypes());
            model.addAttribute("categories", categoryService.getAllCategories());

            return "apps-kanban";
        } else {
            // Handle case where project is not found
            return "projectNotFound"; // Assuming you have a Thymeleaf template named projectNotFound.html
        }
    }


}
