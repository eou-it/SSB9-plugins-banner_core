package  com.sungardhe.banner.testing

import org.springframework.context.ApplicationContext

class InstructorQueryView {
    String termEffective
    Integer pidm
    String facultyID
    String lastName
    String firstName
    String middleInitial
    String facultyCategory
    String facultyStaffType
    String facultyContractType

    def static instructorQueryViewService


    public String toString() {
        """InstructorQueryView[termEffective=$termEffective,
                  pidm=$pidm,
                  facultyID=$facultyID,
                  lastName=$lastName,
                  firstName=$firstName,
                  middleInitial=$middleInitial,
                  facultyCategory=$facultyCategory,
                  facultyStaffType=$facultyStaffType,
                  facultyContractType=$facultyContractType"""
    }


    public static def fetchAll(Map params) {
        if (!params.term) return [list: []]
        if (instructorQueryViewService == null) {
            instructorQueryViewService = InstructorQueryView.getApplicationContext().getBean("instructorQueryViewService")
        }
        def returnObj = [list: instructorQueryViewService.fetchAll(params)]
        return returnObj
    }


    public static def fetchAllPagination(Map params, Map paginationMap) {
        if (!params.term) return [list: []]
        if (instructorQueryViewService == null) {
            instructorQueryViewService = InstructorQueryView.getApplicationContext().getBean("instructorQueryViewService")
        }
        def returnObj = [list: instructorQueryViewService.fetchAllPagination(params, paginationMap)]
        return returnObj
    }
}