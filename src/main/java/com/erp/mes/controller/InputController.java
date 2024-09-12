package com.erp.mes.controller;

import com.erp.mes.dto.InputDTO;
import com.erp.mes.dto.OrderDTO;
import com.erp.mes.dto.PageDTO;
import com.erp.mes.dto.TransactionDTO;
import com.erp.mes.service.InputService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/input")
@Slf4j
public class InputController {
    private final InputService service;

    @ModelAttribute("servletPath")
    String getRequestServletPath(HttpServletRequest request) {
        return request.getServletPath();
    }
    @PostMapping("/inputList")
    public String inputForm(InputDTO inputDTO) {
        int n = service.inputForm(inputDTO);
        if(n > 0) {
            return "input/inputList";
        }else {
            return "401";
        }
    }

    @PostMapping("/inseception")
    public String inseceptionForm(
            @RequestParam("orderCode") String orderCode,
            @RequestParam("inspectionStatus") String inspectionStatus,
            Model model,
            Map<String,Object> map
    ) {
        log.info("a={} " , inspectionStatus);
        map.put("selectValue",inspectionStatus);
        map.put("orderCode",orderCode);
        service.updateInputStatus(map);
        return "redirect:/input/paging";
    }
    // 검색
    @PostMapping("/search")
    public String search(OrderDTO orderDTO,Model model) {
        List<OrderDTO> list = service.serachList(orderDTO);
        log.info("list={}",list);
        model.addAttribute("list",list);
        return "input/search";
    }
    // 구매검색
    @PostMapping("/searchTrans")
    public String searchTrans(OrderDTO orderDTO,Model model) {
        List<OrderDTO> list = service.serachListTrans(orderDTO);
        model.addAttribute("list",list);
        return "input/searchTrans";
    }
    // 입고페이지
    @GetMapping("/paging")
    public String paging(Model model,
                         @RequestParam(value = "page", required = false,defaultValue = "1") int page) {
        List<OrderDTO> pagingList = service.pagingList(page);
        PageDTO pageDTO = service.pagingParam(page);
        log.info("pagin={}",pagingList);
        model.addAttribute("boardList",pagingList);
        model.addAttribute("paging",pageDTO);
        return "input/list";
    }
    // 검수페이지
    @GetMapping("/insep")
    public String insep(Model model,
                        @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        List<OrderDTO> pagingList = service.pagingListTrue(page);

        log.info("={}",pagingList);
        log.info("page={}", page);

        PageDTO pageDTO = service.pagingParamTrue(page);
        model.addAttribute("insep",pagingList);
        model.addAttribute("paging", pageDTO);
        return "input/insep";
    }
    // 구매 페이지
    @GetMapping("/bom")
    public String bom(Model model) {
        List<OrderDTO> orders = service.selectOrders();
        log.info("orders={}",orders);
        model.addAttribute("orders",orders);
        return "input/bom";
    }
    // 거래페이지
    @GetMapping("/transaction")
    public String transaction(Model model) {
        List<OrderDTO> trans = service.selectTran();
        log.info("trans={}",trans);
        List<OrderDTO> transList = service.selectTranList();
        model.addAttribute("trans",trans);
        model.addAttribute("list",transList);
        return "input/transaction";
    }
    @PostMapping("/transaction")
    public String transactionForm(
            @RequestParam("orderCode") String[] orderCode,
            @RequestParam("transSelect") String[] transSelect,
            Map<String, Object> map) {

        if (orderCode == null || orderCode.length == 0 || transSelect == null || transSelect.length == 0 || orderCode.length != transSelect.length) {
            map.put("error", "잘못된 요청입니다.");
            return "error";
        }

        // 상태와 발주코드를 그룹화
        Map<String, List<String>> groupedByStatus = new HashMap<>();
        for (int i = 0; i < orderCode.length; i++) {
            String code = orderCode[i];
            String status = transSelect[i];

            if (code != null && status != null) {
                groupedByStatus
                        .computeIfAbsent(status, k -> new ArrayList<>())
                        .add(code);
            }
        }

        // 발주마감 상태로 일괄 업데이트 처리
        if (groupedByStatus.containsKey("발주마감")) {
            List<String> codesToUpdate = groupedByStatus.get("발주마감");
            int n = service.updateTrans(codesToUpdate);
            log.info("업데이트된 건수: {}", n);
        }

        return "redirect:/input/transaction";
    }

}