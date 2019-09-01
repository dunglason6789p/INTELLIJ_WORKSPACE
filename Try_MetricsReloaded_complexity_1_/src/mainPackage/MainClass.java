package mainPackage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class MainClass {
    public static void main(String[] args) {
        var mainClass = new MainClass();
        mainClass.init();
        mainClass.method6();
    }
    private void init() {
        this.dummyList = new ArrayList<>();
        for(int i=0; i<5; i++) {
            dummyList.add(1);
        }
    }
    List<Integer> dummyList;
    public void method1() {
        int count = 0;
        for(var a:dummyList) {
            for(var b:dummyList) {
                for(var c:dummyList) {
                    for(var d:dummyList) {
                        for(var e:dummyList) {
                            for(var f:dummyList) {
                                for(var g:dummyList) {
                                    //count++;
                                    //method4();
                                    method2();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void method2() {
        int count = 0;
        for(var a:dummyList) {
            for(var b:dummyList) {
                for(var c:dummyList) {
                    for(var d:dummyList) {
                        for(var e:dummyList) {
                            count++;
                        }
                    }
                }
            }
        }
    }
    public void method3() {
        int count = 0;
        for(var a:dummyList) {
            for(var b:dummyList) {
                for(var c:dummyList) {
                    count++;
                }
            }
        }
    }
    public void method4() {
        method1();
    }
    public void method5() {
        int count = 0;
        for(var a:dummyList) {
            for(var b:dummyList) {
                for(var c:dummyList) {
                    for(var d:dummyList) {
                        for(var e:dummyList) {
                            for(var f:dummyList) {
                                for(var g:dummyList) {
                                    //count++;
                                    method4();
                                }
                            }
                        }
                    }
                }
            }
        }
        for(var a:dummyList) {
            for(var b:dummyList) {
                for(var c:dummyList) {
                    for(var d:dummyList) {
                        for(var e:dummyList) {
                            count++;
                        }
                    }
                }
            }
        }
    }
    public void method6() {
        method5();
        method2();
        method3();
    }
}
