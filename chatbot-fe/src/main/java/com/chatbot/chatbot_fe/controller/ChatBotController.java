package com.chatbot.chatbot_fe.controller;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.chatbot_fe.model.DynamicDashboardModel;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
public class ChatBotController {
	


@GetMapping("/check-service")
	public String serviceCheck() {
		return "Hello";
	}

	 @GetMapping("/test")
    public String test() {
        return "dashboard/sampleDashboard";
    }
// @RequestMapping(value = "/", method = RequestMethod.GET)
// public String getEmployeeDashboardData(Model model) throws Exception {

//     List<DynamicDashboardModel> metrics = new ArrayList<>();

//     // Progress widgets
//     metrics.add(new DynamicDashboardModel("Claim Settlement Rate", "percentage", 65, ""));
//     metrics.add(new DynamicDashboardModel("Attendance Rate", "percentage", 78, ""));

//     // Speedometer widgets
//     metrics.add(new DynamicDashboardModel("Training Completion Meter", "speedometer", 80, ""));
//     metrics.add(new DynamicDashboardModel("Event Preparation", "speedometer", 25, ""));

//     // Graph data
//     List<Map<String, Object>> graphData = new ArrayList<>();

//     Map<String, Object> g1 = new HashMap<>();
//     g1.put("label", "2020-2024");
//     g1.put("value", 12);
//     graphData.add(g1);

//     Map<String, Object> g2 = new HashMap<>();
//     g2.put("label", "2025-2029");
//     g2.put("value", 18);
//     graphData.add(g2);

//     Map<String, Object> g3 = new HashMap<>();
//     g3.put("label", "2030-2034");
//     g3.put("value", 9);
//     graphData.add(g3);

//     metrics.add(new DynamicDashboardModel(
//             "Employee count Vs Retirement Year",
//             "graph",
//             graphData,
//             ""
//     ));

//     // OCF Graph
//     List<Map<String, Object>> ocfGraph = new ArrayList<>();

//     Map<String, Object> b1 = new HashMap<>();
//     b1.put("label", "Circular");
//     b1.put("value", 10);
//     ocfGraph.add(b1);

//     Map<String, Object> b2 = new HashMap<>();
//     b2.put("label", "Communication File");
//     b2.put("value", 6);
//     ocfGraph.add(b2);

//     Map<String, Object> b3 = new HashMap<>();
//     b3.put("label", "Office Order");
//     b3.put("value", 14);
//     ocfGraph.add(b3);

//     metrics.add(new DynamicDashboardModel(
//             "Office order Vs Communication File Vs Circular",
//             "graph",
//             ocfGraph,
//             ""
//     ));

//     // ToDo List
//     List<Map<String, Object>> todoList = new ArrayList<>();

//     Map<String, Object> task1 = new HashMap<>();
//     task1.put("task", "Complete dashboard UI");
//     task1.put("status", "pending");
//     task1.put("createdDate", "2026-03-13");
//     todoList.add(task1);

//     Map<String, Object> task2 = new HashMap<>();
//     task2.put("task", "Integrate chatbot API");
//     task2.put("status", "completed");
//     task2.put("createdDate", "2026-03-12");
//     todoList.add(task2);

//     metrics.add(new DynamicDashboardModel(
//             "To-Do List",
//             "todo",
//             todoList,
//             ""
//     ));

//     // Send to UI
//     model.addAttribute("metrics", metrics);

//     // Convert metrics to JSON for JavaScript
//     ObjectMapper mapper = new ObjectMapper();
//     String metricsJson = mapper.writeValueAsString(metrics);

//     model.addAttribute("metricsJson", metricsJson);

//     return "dashboard/sampleDashboard";
// }


}
