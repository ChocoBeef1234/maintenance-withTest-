package main.java.controller;
import main.java.model.Staff;
import main.java.model.Validation;
import main.java.repository.StaffRepository;
import main.java.view.StaffView;
import main.java.config.StaffConstants;
import main.java.config.StaffMenuOption;

public class StaffController {
    private final StaffRepository repository;
    private final StaffView view;

    public StaffController() {
        this.repository = new StaffRepository();
        this.view = new StaffView();
    }

    public void run() {
        boolean back = false;
        while (!back) {
            int selection = view.menu();
            StaffMenuOption option = StaffMenuOption.fromOptionNumber(selection);
            if (option == null) {
                view.info(StaffConstants.MSG_INVALID_INPUT);
                continue;
            }
            
            switch (option) {
                case ADD:
                    handleAdd();
                    break;
                case SEARCH:
                    handleSearch();
                    break;
                case MODIFY:
                    handleModify();
                    break;
                case DELETE:
                    handleDelete();
                    break;
                case BACK:
                    back = true;
                    break;
            }
        }
    }

    private void handleAdd() {
        Staff staff = view.promptNewStaff();
        if (!Validation.isStaffId(staff.getStaffId())) {
            view.info(StaffConstants.MSG_INVALID_STAFF_ID_FORMAT);
            return;
        }
        try {
            boolean added = repository.add(staff);
            if (added) {
                view.info(StaffConstants.MSG_STAFF_ADDED_SUCCESS);
            } else {
                view.info(StaffConstants.MSG_STAFF_ID_EXISTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info(StaffConstants.MSG_FAILED_TO_ADD);
        }
    }

    private void handleSearch() {
        String id = view.promptStaffId(StaffConstants.PROMPT_STAFF_ID_SEARCH);
        try {
            Staff staff = repository.findById(id);
            if (staff != null) {
                view.showStaff(staff);
            } else {
                view.info(StaffConstants.MSG_STAFF_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info(StaffConstants.MSG_FAILED_TO_SEARCH);
        }
    }

    private void handleModify() {
        String id = view.promptStaffId(StaffConstants.PROMPT_STAFF_ID_MODIFY);
        try {
            Staff current = repository.findById(id);
            if (current == null) {
                view.info(StaffConstants.MSG_STAFF_NOT_FOUND);
                return;
            }
            Staff updated = view.promptUpdate(current);
            boolean ok = repository.update(current.getStaffId(), updated);
            if (ok) {
                view.info(StaffConstants.MSG_STAFF_MODIFIED_SUCCESS);
            } else {
                view.info(StaffConstants.MSG_FAILED_TO_MODIFY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info(StaffConstants.MSG_FAILED_TO_MODIFY);
        }
    }

    private void handleDelete() {
        String id = view.promptStaffId(StaffConstants.PROMPT_STAFF_ID_DELETE);
        try {
            boolean ok = repository.delete(id);
            if (ok) {
                view.info(StaffConstants.MSG_STAFF_DELETED_SUCCESS);
            } else {
                view.info(StaffConstants.MSG_STAFF_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info(StaffConstants.MSG_FAILED_TO_DELETE);
        }
    }
}

