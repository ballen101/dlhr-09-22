/**
 * Created by Administrator on 2014-11-02.
 */
var mainline = undefined;
var frmOptions = undefined, formetype = undefined, wfform = undefined;
$.parser.auto = false;

$(document).ready(function () {
        //传入参数   formetype   1制单 2 审批 3 浏览 4 变更
        //id      JPA数据ID 如果有传入，将在界面初始化后载入该JPA数据
        //wfform  作为wfform显示 true 显示 false不显示
        if (frmOptions && (!mainline)) {
            var parms = $getPageParms();
            formetype = parms.formetype;
            frmOptions.initid = parms.id;
            wfform = parms.wfform;
            if ((wfform) && (wfform.toUpperCase != 'TRUE'))
                frmOptions.allowWF = false;
            mainline = new TMainLine(frmOptions);
        }
    }
);

var TEditType = {etEdit: 1, etSubmit: 2, etView: 3, etUpdate: 4, etFind: 6};
var TFrmType = {ftMainLine: 1, ftSimple: 2};

var TButtons = {
    btNew: {
        disable: function () {
            $("#id_bt_new").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_new").linkbutton("enable")
        }
    },
    btCopy: {
        disable: function () {
            $("#id_bt_copy").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_copy").linkbutton("enable")
        }
    },
    btSave: {
        disable: function () {
            $("#id_bt_save").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_save").linkbutton("enable")
        }
    },
    btSubmit: {
        disable: function () {
            $("#id_bt_submit").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_submit").linkbutton("enable")
        }
    },
    btFind: {
        disable: function () {
            $("#id_bt_find").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_find").linkbutton("enable")
        }
    },
    //   btDeatil: {
    //       disable: function () {
    //           $("#id_bt_detail").linkbutton("disable")
    //       },
    //       enable: function () {
    //           $("#id_bt_detail").linkbutton("enable")
    //       }
    //   },
    btReload: {
        disable: function () {
            $("#id_bt_reload").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_reload").linkbutton("enable")
        }
    },
    btDel: {
        disable: function () {
            $("#id_bt_del").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_del").linkbutton("enable")
        }
    },
    btAtt: {
        disable: function () {
            $("#id_bt_att").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_att").linkbutton("enable")
        }
    },
    btPrint: {
        disable: function () {
            $("#id_bt_print").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_print").linkbutton("enable")
        }
    },
    btExpt: {
        disable: function () {
            $("#id_bt_expt").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_expt").linkbutton("enable")
        }
    },
    btExit: {
        disable: function () {
            $("#id_bt_exit").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_exit").linkbutton("enable")
        }
    },
    btDelAtt: {
        disable: function () {
            $("#id_bt_delatt").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_delatt").linkbutton("enable")
        }
    },
    btDownloadAtt: {
        disable: function () {
            $("#id_bt_downloadatt").linkbutton("disable")
        },
        enable: function () {
            $("#id_bt_downloadatt").linkbutton("enable")
        }
    },
    setClick: function (onClick) {
        $("#id_bt_new").linkbutton({onClick: function () {
            onClick($C.action.New);
        }});
        $("#id_bt_copy").linkbutton({onClick: function () {
            onClick($C.action.Copy);
        }});
        $("#id_bt_save").linkbutton({onClick: function () {
            onClick($C.action.Save);
        }});
        $("#id_bt_submit").linkbutton({onClick: function () {
            onClick($C.action.Submit);
        }});
        $("#id_bt_find").linkbutton({onClick: function () {
            onClick($C.action.Find);
        }});
        // $("#id_bt_detail").linkbutton({onClick: function () {
        //     onClick($C.action.Detail);
        // }});
        $("#id_bt_reload").linkbutton({onClick: function () {
            onClick($C.action.Reload);
        }});
        $("#id_bt_del").linkbutton({onClick: function () {
            onClick($C.action.Del);
        }});
        $("#id_bt_print").linkbutton({onClick: function () {
            onClick($C.action.Print);
        }});
        $("#id_bt_expt").linkbutton({onClick: function () {
            onClick($C.action.Export);
        }});
        $("#id_bt_exit").linkbutton({onClick: function () {
            onClick($C.action.Exit);
        }});
        $("#id_bt_att").linkbutton({onClick: function () {
            onClick($C.action.Upload);
        }});
        $("#id_bt_delatt").linkbutton({onClick: function () {
            onClick($C.action.DelAtt);
            //alert("asd");
            //deleteattfile()
        }});
        $("#id_bt_downloadatt").linkbutton({onClick: function () {
            onClick($C.action.Download);
            //rightClickDownload();
        }});
    }
};

function rightClickDownload() {
    var row = $("#dg_att_id").datagrid('getSelected');
    if (row) {
        var furl = $C.cos.downattfile() + "?pfid=" + row.pfid;
        window.open(furl);
    } else {
        $.messager.alert('错误', '没有选择附件!', 'error');
    }
}

function deleteattfile() {
    var row = $("#dg_att_id").datagrid('getSelected');
    if (row) {
        var furl = $C.cos.delattfile() + "?pfid=" + row.pfid;
        // window.open(furl);
        $ajaxjsonget(furl, function () {
            $C.grid.remove("#dg_att_id");
            // alert("删除成功");
        }, function () {
            $.messager.alert('错误', '删除附件失败!', 'error');
        });
    } else {
        $.messager.alert('错误', '没有选择附件!', 'error');
    }
}

function TMainLine(afrmOptions) {
    if (!afrmOptions) {
        $.messager.alert('错误', '没有设置frmOptions变量!', 'error');
        return undefined;
    }
    var datachanged = false;
    this.getDataChanged = function () {
        return datachanged;
    }
    this.setDataChanged = function (value) {
        datachanged = value;
    }
    var frmOptions = afrmOptions;
    var copyFind_pw = undefined;
    var curMainData = undefined;
    var lineToolBar = [
        {
            text: '添加行',
            iconCls: 'icon-add1',
            handler: function () {
                if (frmOptions.onAddLine)
                    frmOptions.onAddLine(append)
                else
                    $C.grid.append("detail_main_grid_id", {}, true)
                function append(rowdata) {
                    $C.grid.append("detail_main_grid_id", rowdata, true);
                }
            }
        },
        "-",
        {
            text: '删除行',
            iconCls: 'icon-remove',
            handler: function () {
                var rowdata = $("#detail_main_grid_id").datagrid("getSelected");
                if (!rowdata) {
                    $.messager.alert('错误', '没有选择的数据！', 'error');
                    return;
                }
                if (frmOptions.onRemoveLine) {
                    if (frmOptions.onRemoveLine(rowdata)) {
                        $C.grid.remove("detail_main_grid_id");
                    }
                } else {
                    $C.grid.remove("detail_main_grid_id");
                }
            }
        }
    ];
    var edittype = getEditType();
    var isloadingdata = false;
    var isnew = false;
    var els_readonly = []; //初始化时 保存界面上所有只读 输入框
    var added_grid_ids = [];

    var frmtype = undefined;
    var allowWF = undefined;
    var allowAtt = undefined;
    var gdLinesName = undefined;
    var lineHeight = 200;

    ////类函数

    readOptions(afrmOptions);

    if (frmOptions.gdCbxUrls)
        $C.grid.initComFormaters({comUrls: frmOptions.gdCbxUrls, onOK: function () {
            setTimeout(addlinegrid, 500);
        }});
    else
        addlinegrid();


    function addlinegrid() {
        if (frmtype == TFrmType.ftMainLine) {
            $("#detail_main_layout_id").css("height", lineHeight + "px");
            getHtmTemp();
        } else
            getHtmTemp();
    }

    function getHtmTemp() {
        $("#main_tabs_id").tabs("select", 1);
        $("#main_tabs_id").tabs("select", 0);
        if (frmtype == TFrmType.ftMainLine) {
            $("#detail_main_grid_id").datagrid({singleSelect: true, rownumbers: true, fit: true, onClickRow: function (index, row) {
                $C.grid.accept("detail_main_grid_id");
            }, onDblClickRow: function (index, row) {
                if ((edittype == TEditType.etEdit) || (edittype == TEditType.etUpdate))
                    $C.grid.edit("detail_main_grid_id");
            },
                border: false,
                columns: [frmOptions.gdLinesColumns()]
            });
            if ((edittype == TEditType.etSubmit) || (edittype == TEditType.etView)) {
                $("#detail_main_grid_id").datagrid({toolbar: undefined});
            } else {
                $("#detail_main_grid_id").datagrid({toolbar: lineToolBar});
            }
        } else {
            $('#main_tab_common_layout_id').layout('panel', 'north').panel('panel').removeClass('layout-split-north');
            $('#main_tab_common_layout_id').layout('panel', 'north').panel('resize');
        }

        initUI();

        if (edittype == TEditType.etEdit)
            donew();
        onReady();
        if (frmOptions.initid)
            findDetail(frmOptions.initid);
        if (wfform) {
            //   $("#main_tabs_id").tabs("close", "列表");
        }
    }

    function onReady() {
        if (frmOptions.OnReady) {
            frmOptions.OnReady();
        }
    }

    function initUI() {
        TButtons.setClick(OnBtClick);//设置按钮事件
        initBts();//设置按钮enable
        initMainTabs();//设置主页
        initInput();//设置输入框
        initGrids();//设置Grid
        function initBts() {
            setBtsDisable();
            TButtons.btFind.enable();
            TButtons.btExpt.enable();
            TButtons.btExit.enable();
            //TButtons.btDeatil.enable();
            TButtons.btReload.enable();
            TButtons.btPrint.enable();
            if (wfform) {
                TButtons.btFind.disable();
            }
            if ((edittype == TEditType.etEdit) || (edittype == TEditType.etUpdate)) {
                TButtons.btSave.enable();
                if (allowAtt) {
                    TButtons.btAtt.enable();
                    TButtons.btDelAtt.enable();
                    TButtons.btDownloadAtt.enable();
                }
                if (edittype == TEditType.etEdit) {
                    TButtons.btNew.enable();
                    TButtons.btDel.enable();
                    TButtons.btCopy.enable();
                    TButtons.btSubmit.enable();
                    if (allowWF) {
                        TButtons.btSubmit.enable();
                    }
                }
            }

            if (edittype == TEditType.etSubmit) {
                // TButtons.btSave.enable();
                if (allowWF) {

                }
                if (allowAtt) {
                    // TButtons.btAtt.enable();
                    // TButtons.btDelAtt.enable();
                    TButtons.btDownloadAtt.enable();
                }
            }

            if (edittype == TEditType.etView) {
                if (allowWF) {
                }
                if (allowAtt) {
                    // TButtons.btAtt.enable();
                    TButtons.btDownloadAtt.enable();
                }
            }
            if (frmOptions.onSetButtonts) {
                frmOptions.onSetButtonts();
            }
        }

        function initMainTabs() {
            if (!allowWF) {
                $("#main_tabs_id").tabs("close", "流程");
            }
            if (!allowAtt) {
                $("#main_tabs_id").tabs("close", "附件");
            }
            if (frmtype == TFrmType.ftSimple) {
                var mpanel = $("#main_tab_common_layout_id").layout("panel", "north");
                var lpanel = $("#main_tab_common_layout_id").layout("panel", "center");
                mpanel.panel({region: "center"});
                lpanel.panel({region: "south"});
                $.parser.parse('#main_tab_common_id');
                $("#main_tab_common_layout_id").layout("remove", "center");
            }
        }

        function initInput() {
            getInputReadOnlys();
            $("#main_tabs_id").find("input[cjoptions]").each(function (index, el) {
                var co = $.c_parseOptions($(this));

                if (co.dicgpid != undefined) {
                    var obj = $(this);
                    obj.combobox({onChange: onValueChange});
                    $ajaxjsonget(_serUrl + "/web/dict/getdictvalues.co?dicid=" + co.dicgpid,
                        function (jsondata) {
                            if (frmOptions.onInitInputComboboxDict) {
                                frmOptions.onInitInputComboboxDict(co, jsondata);
                            }
                            obj.combobox({
                                data: jsondata,
                                valueField: 'dictvalue',
                                textField: 'language1'
                            });
                        }, function () {
                            obj.combobox({
                                data: {},
                                valueField: 'dictvalue',
                                textField: 'language1'
                            });
                            $.messager.alert("错误", "获取数据字典错误!", "error");
                        }, false);
                } else if (co.comidx != undefined) {
                    var obj = $(this);
                    obj.combobox({onChange: onValueChange});
                    var comurl = $C.grid.comDatas[co.comidx];
                    if (comurl != undefined) {
                        if (frmOptions.onInitInputComboboxDict) {
                            frmOptions.onInitInputComboboxDict(co, comurl.data);
                        }
                        if (comurl.type == "combobox") {
                            obj.combobox({
                                data: comurl.data,
                                valueField: comurl.valueField,
                                textField: comurl.textField
                            });
                        }
                        if (comurl.type == "combotree") {
                            obj.tree({
                                data: comurl.data
                            });
                        }
                    }
                } else {
                    var obj = $(this);
                    obj.textbox({onChange: onValueChange});
                }


                if (co.required) {
                    var tv = getInputLabel("#main_tabs_id", co.fdname);//$(this).parent().prev();
                    if (tv)
                        tv.html(tv.html() + "(<span style='color: red'>*</span>)");
                    // else
                    //    alert(co.fdname);
                }
                if ((co.readonly) || (edittype == TEditType.etSubmit) || (edittype == TEditType.etView)) {
                    setInputReadOnly($(this), true);
                }
            });
        }

        function initGrids() {
            $("#dg_att_id").datagrid({
                border: false,
                columns: [
                    [
                        {field: 'displayfname', title: '附件名', width: 200},
                        {field: 'extname', title: '扩展名', width: 100},
                        {field: 'filesize', title: '大小', width: 100},
                        {field: 'filevision', title: '版本', width: 100},
                        {field: 'filecreator', title: '所有者', width: 100},
                        {field: 'filecreate_time', title: '创建时间', width: 150}
                    ]
                ],
                onDblClickRow: onAttGrigDBClick,
                onRowContextMenu: onRowRightClick
            });
            if (!frmOptions.gdListColumns) {
                $.messager.alert('错误', '没有设置列表页字段!', 'error');
            }
            $("#dg_datalist_id").datagrid({
                border: false,
                onDblClickRow: onListDbClick,
                columns: [frmOptions.gdListColumns()]
            });
            inipageins("#dg_datalist_id");

            if (frmOptions.afterEditGrid) {
                $("#dg_att_id").datagrid({
                    onAfterEdit: function (index, row, changes) {
                        frmOptions.afterEditGrid("#dg_att_id", index, row, changes);
                    }
                });
                $("#dg_datalist_id").datagrid({onAfterEdit: function (index, row, changes) {
                    frmOptions.afterEditGrid("#dg_datalist_id", index, row, changes);
                }});
                if (frmOptions.gdLinesColumns) {
                    $("#detail_main_grid_id").datagrid({onAfterEdit: function (index, row, changes) {
                        frmOptions.afterEditGrid("#detail_main_grid_id", index, row, changes);
                    }});
                }
            }

        }
    }

    this.appendLineGrid = appendLineGrid;
    function appendLineGrid(Options) {
        //title
        //gridID
        // columns;
        if (frmtype == TFrmType.ftSimple) return undefined;
        var id = new Date().format("hhmmss") + "_id";
        var gdid = undefined;
        if (Options.gridID)
            gdid = Options.gridID;
        if ((!gdid) || (gdid.length == 0))
            gdid = "gd_" + id;
        $("#detail_main_tabs_id").tabs("add", {
            id: "tab_" + id,
            title: Options.title,
            content: "<table id='" + gdid + "'></table>"
        });
        $("#" + gdid).datagrid({
            border: false,
            columns: [Options.columns]
        });

        if (frmOptions.afterEditGrid) {
            $("#" + gdid).datagrid({
                onAfterEdit: function (index, row, changes) {
                    frmOptions.afterEditGrid("#" + gdid, index, row, changes);
                }
            });
        }
        added_grid_ids.push(gdid);
        return {tabid: "tab_" + id, gdid: gdid};
    }

    function setBtsDisable() {
        for (var i in TButtons) {
            if (TButtons[i].disable)
                TButtons[i].disable();
        }
    }

    function OnBtClick(act) {
        switch (act) {
            case $C.action.Find:
                find();
                break;
            case $C.action.New:
                donew();
                break;
            case $C.action.Copy:
                docopy();
                break;
            case $C.action.Save:
                dosave();
                break;
            case $C.action.Del:
                dodel();
                break;
            case $C.action.Submit:
                dosubmit();
                break;
            case $C.action.Print:
                doprint();
                break;
            case $C.action.Exit:
                if (frmOptions.onExit)
                    frmOptions.onExit();
                break;
            case $C.action.Upload:
                doupload();
                break;
            case $C.action.Download:
                rightClickDownload();
                break;
            case $C.action.DelAtt:
                deleteattfile();
                break;

        }
    }

    function clearViewData() {
        if (frmtype == TFrmType.ftMainLine) {
            var grids = (gdLinesName) ? added_grid_ids.concat(["detail_main_grid_id"]) : added_grid_ids;
            JSONBandingFrm.clearMainData("#main_tabs_id", grids);
        } else
            JSONBandingFrm.clearMainData("#main_tabs_id", []);
    }

////  new
    function donew() {
        if (datachanged) {
            $.messager.confirm('提示', '忽略数据修改?', function (r) {
                if (r) {
                    do_new();
                }
            });
        } else
            do_new();
        return true;
    }

    function do_new() {
        reSetInputReadOnly();
        isnew = true;
        clearViewData();
        curMainData = {};
        $("#main_tabs_id").c_setJsonDefaultValue(curMainData, true);
        if (frmOptions.onNew)
            frmOptions.onNew(curMainData);
        showDetail();
    }

////copy
    function docopy() {
        if (!copyFind_pw) {
            var copyFind_pw_options = {
                id: "copyFind_pw",
                enableIdpath: true,
                JPAClass: frmOptions.JPAClass,  //对应后台JPAClass名
                gdListColumns: frmOptions.gdListColumns(),
                chgidpath: true,
                selfLine: false,
                edittype: TEditType.etFind,
                beforeFind: function (fdparms) {
                    fdparms.chgidpath = true;
                    return true;
                },
                onResult: function (rows) {
                    if (rows.length > 0) {
                        var jpa = rows[0];
                        //jpaclass  id clearfields
                        var parms = {};
                        parms.jpaclass = frmOptions.JPAClass;
                        parms.id = jpa[frmOptions.JPAIdField];
                        if (frmOptions.onCopy) {
                            frmOptions.onCopy(parms);
                        }
                        $ajaxjsonpost($C.cos.commonCopy, JSON.stringify(parms), function (jsdata) {
                            $("#dg_datalist_id").datagrid("appendRow", jsdata);
                            var id = jsdata[frmOptions.JPAIdField];
                            if ((!id) || (id.length == 0)) {
                                alert("列表数据没发现ID值");
                                return;
                            }
                            findDetail(id);
                        }, function (msg) {
                            $.messager.alert('错误', '复制错误:' + msg, 'error');
                        });
                    }
                }
            };
            copyFind_pw = new TSearchForm(copyFind_pw_options);
        }
        copyFind_pw.show();
    }

//save
    function dosave() {
        curMainData = getMainData();
        if (!JSONBandingFrm.checkNotNull("#main_tabs_id", curMainData)) return false;

        //////save
        var surl = ((frmOptions.saveUrl) && (frmOptions.saveUrl.length > 0)) ? frmOptions.saveUrl : $C.cos.commonsave;
        var postData = {jpaclass: frmOptions.JPAClass, lines: true, jpadata: curMainData};
        if (frmOptions.onSave) {
            if (!frmOptions.onSave(postData)) return false;
        }
        $ajaxjsonpost(surl, JSON.stringify(postData), function (jdata) {
            curMainData = jdata;
            if (isnew) {
                $("#dg_datalist_id").datagrid("appendRow", jdata);
            }
            showDetail();
        }, function (msg) {
            $.messager.alert('错误', '保存数据错误:' + msg, 'error');
        });
    }

    function dodel() {
        curMainData = getMainData();
        var id = (curMainData[frmOptions.JPAIdField]);
        if ((!id) || (id.length == 0)) {
            $.messager.alert('错误', '未保存的或空数据，不需要删除！', 'error');
            return;
        }
        $.messager.confirm('确认', '确认删除当前记录?', function (r) {
            if (r) {
                var surl = ((frmOptions.delUrl) && (frmOptions.delUrl.length > 0)) ? frmOptions.delUrl : $C.cos.commondelete;
                surl = surl + "?jpaclass=" + frmOptions.JPAClass + "&id=" + id;
                $ajaxjsonget(surl, function (jdata) {
                    if (jdata.result == "OK") {
                        $C.grid.removeByField("#dg_datalist_id", id, frmOptions.JPAIdField);
                        donew();//成功删除后，清除界面，恢复新建状态
                    } else {
                        $.messager.alert('错误', '删除失败:' + jdata.result, 'error');
                    }

                }, function (msg) {
                    $.messager.alert('错误', '删除失败:' + msg, 'error');
                });
            }
        });
    }

    var pw_SearchWT = undefined;

    function dosubmit() {
        curMainData = getMainData();
        var jpaid = (curMainData[frmOptions.JPAIdField]);
        if ((!jpaid) || (jpaid.length == 0)) {
            $.messager.alert('错误', '未保存的或空数据，不能提交！', 'error');
            return;
        }
        if (datachanged) {
            $.messager.alert('错误', '请先保存单据！', 'error');
            return;
        }
        $.messager.confirm('确认', '确认提交当前单据?', function (r) {
            if (r) {
                var pwOption = {
                    id: "pws_selectWFTem",
                    JPAClass: "com.corsair.server.wordflow.Shwwftemp",  //对应后台JPAClass名
                    gdListColumns: [
                        {field: 'wftempname', title: '流程名称', width: 150}
                    ],
                    singleSelect: true,
                    autoFind: true, singleAutoReturn: true, notFindAutoReturn: true,
                    edittype: TEditType.etView,
                    enableIdpath: false,
                    beforeFind: function (parms) {
                        parms.url = _serUrl + "/web/common/findWfTemps.co";
                        parms.jpaclass = frmOptions.JPAClass;
                        parms.id = jpaid;
                        return true;
                    },
                    onResult: function (rows) {
                        var wftempid = (rows.length == 0) ? undefined : rows[0].wftempid;
                        var parms = {jpaclass: frmOptions.JPAClass, jpaid: jpaid};
                        if (wftempid)
                            parms.wftempid = wftempid;
                        $ajaxjsonpost($C.cos.commoncreateWF, JSON.stringify(parms), function (jsdata) {
                            curMainData = jsdata;
                            showDetail();
                        }, function (msg) {
                            $.messager.alert('错误', '提交单据错误:' + msg, 'error');
                        });
                    }
                };
                if (pw_SearchWT) {
                    pw_SearchWT.TSearchForm(pwOption);
                } else {
                    pw_SearchWT = new TSearchForm(pwOption);
                }
                pw_SearchWT.show();
            }
        });
    }

////find begin
    this.find_window_ok_click = function () {
        $C.grid.accept("dg_find_window_parms_id");
        var parms = $("#dg_find_window_parms_id").datagrid("getData").rows;
        do_find(parms);
    }

    function find() {
        var peditor = {
            type: 'combobox',
            options: {
                valueField: 'field', textField: 'title', data: frmOptions.gdListColumns()
            }
        };
        var pft = function (value, row) {
            var dt = frmOptions.gdListColumns;
            for (var i = 0; i < dt.length; i++) {
                var r = dt[i];
                if (value == r.field) {
                    return r.title;
                }
            }
            return value;
        };
        var redtor = {
            type: 'combobox',
            options: {
                valueField: 'id', textField: 'value',
                data: [
                    {id: 'like', value: 'like'},
                    {id: '=', value: '='},
                    {id: '>', value: '>'},
                    {id: '<', value: '<'}
                ]}
        };

        $("#dg_find_window_parms_id").datagrid({columns: [
            [
                {field: 'parmname', title: '参数名', width: 100, editor: peditor, formatter: pft},
                {field: 'reloper', title: '运算关系', width: 60, editor: redtor},
                {field: 'parmvalue', title: '参数值', width: 100, editor: 'text'}
            ]
        ]});
        $C.grid.clearData('dg_find_window_parms_id');
        $C.grid.append('dg_find_window_parms_id', {}, true);
        $("#find_window_id").window("open");
    }

    function inipageins(gdfilter) {
        var gd = $(gdfilter);
        gd.datagrid({
            // pageNumber: 1,
            pagination: true,
            rownumbers: true,
            pageSize: 30,
            pageList: [30, 50, 100, 300]
        });

        var p = gd.datagrid('getPager');
        p.pagination({
            beforePageText: '第',//页数文本框前显示的汉字
            afterPageText: '页    共 {pages} 页',
            displayMsg: '共{total}条数据'
        });
    }

    function do_find(parms) {
        for (var i = parms.length - 1; i >= 0; i--) {
            if ((!parms[i].parmname) || (parms[i].parmname.length == 0)) {
                parms.splice(i, 1);
            }
        }
        var url = $C.cos.commonfind + "?type=list&chgidpath=true&edittype=" + edittype + "&jpaclass=" + frmOptions.JPAClass;
        if (parms.length > 0)
            url = url + "&parms=" + JSON.stringify(parms);
        //console.error(url);
        var gd = $("#dg_datalist_id");
        var p = gd.datagrid("getPager");
        setOnListselectPage(url);
        var option = p.pagination("options");
        do_getdata(url, option.pageNumber, option.pageSize);
    }

    function setOnListselectPage(url) {
        var gd = $("#dg_datalist_id");
        var p = gd.datagrid("getPager");
        p.pagination({
            onSelectPage: function (pageNumber, pageSize) {
                do_getdata(url, pageNumber, pageSize);
            }
        });
    }

    function do_getdata(url, page, pageSize) {
        $ajaxjsonget(url + "&page=" + page + "&rows=" + pageSize, function (jsdata) {
            $C.grid.clearData("dg_datalist_id");
            $("#dg_datalist_id").datagrid({pageNumber: page, pageSize: pageSize});
            setOnListselectPage(url);
            $("#dg_datalist_id").datagrid("loadData", jsdata);
            $("#main_tabs_id").tabs("select", "列表");
            $("#find_window_id").window("close");
        }, function () {
            //console.error("查询错误");
        });
    }


    function onListDbClick(index, row) {
        if (!frmOptions.JPAIdField) {
            alert("没有设置JPA ID字段名");
            return;
        }
        var id = row[frmOptions.JPAIdField];
        if ((!id) || (id.length == 0)) {
            alert("列表数据没发现ID值");
            return;
        }
        findDetail(id);
    }

    function onRowRightClick(e, rowIndex, rowData) {
        e.preventDefault();
        $('#ContextMenu').menu('show', {
            left: e.pageX,
            top: e.pageY
        });
    }

    this.findDetail = findDetail;
    function findDetail(id) {
        if (datachanged) {
            $.messager.confirm('提示', '忽略数据修改?', function (r) {
                if (r) {
                    find_detail(id);
                }
            });
        } else
            find_detail(id);
        return true;
    }

    function find_detail(id) {
        curMainData = {};
        var url = $C.cos.commonfind + "?type=byid&chgidpath=true&id=" + id + "&jpaclass=" + frmOptions.JPAClass;
        $ajaxjsonget(url, function (jsdata) {
            isnew = false;
            curMainData = jsdata;
            showDetail();
        }, function () {
            alert("查询数据错误");
        });
    }

    this.showDetail = showDetail;
    function showDetail() {
        isloadingdata = true;
        //先改界面显示 再写入值，否则会消失，应该是bug
        if ((!$isEmpty(curMainData.stat)) && (parseInt(curMainData.stat) > 1) && (edittype != TEditType.etUpdate)) {
            setAllInputReadOnly(true);
            showWFinfo();//如果有流程  根据流程刷新可以编辑的输入框
        } else {
            $("#main_tab_wf_id").html("");
            $("#main_tabs_id").tabs("select", "常规");
        }
        setJson2Inputs();
        setJson2Grids();
        isloadingdata = false;
        datachanged = false;
    }

    function showWFinfo() {
        if (!$isEmpty(curMainData.wfid)) {
            $("#main_tabs_id").tabs("select", "流程");
            var src = _serUrl + "/webapp/common/shwwf.html?wfid=" + curMainData.wfid + "&showform=false";
            var wfifrm = "<iframe scrolling='no' frameborder='0' src='" + src + "' style='width: 100%;height: 98%'></iframe>";
            $("#main_tab_wf_id").html(wfifrm);
            //$("#main_tabs_id").tabs("select", "常规");
        } else {
            $("#main_tabs_id").tabs("select", "常规");
            $("#main_tab_wf_id").html("");
        }
    }

    this.setJson2Inputs = setJson2Inputs;
    function setJson2Inputs() {
        return JSONBandingFrm.fromJsonData("#maindata_id", curMainData)
    }

    this.setJson2Grids = setJson2Grids;
    function setJson2Grids() {
        if (frmtype == TFrmType.ftMainLine) {
            if (curMainData[gdLinesName]) {
                $("#detail_main_grid_id").datagrid("loadData", curMainData[gdLinesName]);
            }
            else $C.grid.clearData("detail_main_grid_id");

            for (var i = 0; i < added_grid_ids.length; i++) {
                var grid_id = added_grid_ids[i];
                try {
                    if (curMainData[grid_id])
                        $("#" + grid_id).datagrid("loadData", curMainData[grid_id]);
                    else
                        $C.grid.clearData(grid_id);
                }
                catch (e) {

                }
            }
        }
        if (allowAtt) {
            if ((curMainData.shw_attachs) && (curMainData.shw_attachs.length > 0) && (curMainData.shw_attachs[0].shw_attach_lines)) {
                $("#dg_att_id").datagrid("loadData", curMainData.shw_attachs[0].shw_attach_lines);
            } else {
                $C.grid.clearData("dg_att_id");
            }
        }
    }


    function doprint() {
        curMainData = getMainData();
        var jpaid = (curMainData[frmOptions.JPAIdField]);
        if ((!jpaid) || (jpaid.length == 0)) {
            $.messager.alert('错误', '未保存的或空数据，不能提交！', 'error');
            return;
        }
        var url = _serUrl + "/web/common/findModels.co?jpaclass=" + frmOptions.JPAClass;
        $("#pw_list_select").c_popselectList(url, function (fi) {
            if (fi) {
                var url = $C.cos.commondowloadExcelByModel + "?jpaclass=" + frmOptions.JPAClass + "&modfilename=" + fi.fname
                    + "&jpaid=" + getid();
                window.open(url);
            }
        });

    }

//find end

    ///upload att
    function doupload() {
        curMainData = getMainData();
        var id = (curMainData[frmOptions.JPAIdField]);
        if ((!id) || (id.length == 0)) {
            $.messager.alert('错误', '未保存的或空数据，不允许上传附件！', 'error');
            return;
        }
        if (curMainData.shw_attachs == undefined) {
            $.messager.alert('错误', 'JPA没有设置附件属性，不允许上传附件！', 'error');
            return;
        }

        var wd = $("#pw_uploadfile iframe");
        wd.attr("src", _serUrl + "/webapp/templet/default/uploadfile.html?action=" + $C.cos.commonUpLoadFile);
        window.setTimeout(function () {
            wd[0].contentWindow._callback = function (rst) {
                var Shw_attach = ((curMainData.shw_attachs) && (curMainData.shw_attachs.length > 0)) ? curMainData.shw_attachs[0] : {};
                if (!Shw_attach.shw_attach_lines) {
                    Shw_attach.shw_attach_lines = [];
                }
                try {
                    var pfs = eval(rst);
                    for (var i = 0; i < pfs.length; i++) {
                        pf = pfs[i];
                        Shw_attach.shw_attach_lines.push({pfid: pf.pfid, fdrid: 0, displayfname: pf.displayfname, extname: pf.extname, filesize: pf.filesize});
                    }
                    curMainData.shw_attachs = [Shw_attach];
                    dosave();//上传附件后 需要保存对象
                    $("#pw_uploadfile").window("close");
                } catch (e) {
                    if (rst.indexOf("Request Entity Too Large") > -1) {
                        $.messager.alert('上传文件错误', "文件超大", 'error');
                    } else {
                        $.messager.alert('上传文件错误', rst, 'error');
                    }
                }
            };
        }, 500);
        $("#pw_uploadfile").window("open");
    }

    function onAttGrigDBClick(index, row) {
        var furl = $C.cos.downattfile() + "?pfid=" + row.pfid;
        window.open(furl);
    }


    //public proterty
    this.isnew = function () {
        return isnew;
    }
    this.isloadingdata = function () {
        return isloadingdata;
    }

    this.getMainData = getMainData;
    function getMainData() {
        curMainData = JSONBandingFrm.toJsonData("#main_tabs_id", curMainData, isnew);
        if ((frmtype == TFrmType.ftMainLine) && gdLinesName) {
            var lines = $("#detail_main_grid_id").datagrid("getRows");
            curMainData[gdLinesName] = lines;
            for (var i = 0; i < added_grid_ids.length; i++) {
                try {
                    var grid_id = added_grid_ids[i];
                    var lines = $("#" + grid_id).datagrid("getRows");
                    curMainData[grid_id] = lines;
                }
                catch (e) {

                }
            }
        }
        return curMainData;
    }

    this.lineToolBar = function () {
        return lineToolBar;
    };

    this.getid = getid;
    function getid() {
        return curMainData[frmOptions.JPAIdField];
    }

    this.getEditType = function () {
        return edittype;
    }

    this.setEditType = function (value) {
        edittype = value;
    }


    //other functions


    function readOptions(aFrmOptions) {
        frmOptions.frmType = (aFrmOptions.frmType) ? aFrmOptions.frmType : TFrmType.ftMainLine;
        frmOptions.allowWF = (aFrmOptions.allowWF != undefined) ? aFrmOptions.allowWF : true;
        frmOptions.allowAtt = (aFrmOptions.allowAtt != undefined) ? aFrmOptions.allowAtt : true;
        frmOptions.gdLinesName = (aFrmOptions.gdLinesName) ? aFrmOptions.gdLinesName : new Date().format("hhmmss") + "_id";
        frmOptions.lineHeight = ( aFrmOptions.lineHeight) ? aFrmOptions.lineHeight : 200;


        frmtype = frmOptions.frmType;
        allowWF = frmOptions.allowWF;
        allowAtt = frmOptions.allowAtt;
        gdLinesName = frmOptions.gdLinesName;
        lineHeight = frmOptions.lineHeight;
    }

    function getEditType() {
        if (parseInt(formetype) == 1)
            return TEditType.etEdit;
        else if (parseInt(formetype) == 2)
            return TEditType.etSubmit;
        else if (parseInt(formetype) == 3)
            return TEditType.etView;
        else if (parseInt(formetype) == 4)
            return TEditType.etUpdate;
        else
            return TEditType.etView;
    }

    this.setReadOnly = function (fdname, readonly) {
        var v = undefined;
        var ipt = undefined;
        $("#maindata_id").find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            var et = getInputType(el);
            if (co.fdname == fdname) {
                ipt = $(this);
                v = $(this).textbox("getValue");
                return false;
            }
        });
        setInputReadOnly(ipt, readonly);
        $setInputValue("#maindata_id", fdname, v);
    }

    function setInputReadOnly(input, readonly) {
        //alert(input.next().find("input:first").parent().html());
        var tp = getInputType(input[0]);
        if (tp == 1)
            input.datetimebox({readonly: readonly});
        if (tp == 2)
            input.combobox({readonly: readonly});
        if (tp == 5)
            input.textbox({readonly: readonly});
        if (readonly) {
            input.next().find("input:first").css("background-color", "#E8EEFA");
            input.next().find("textarea:first").css("background-color", "#E8EEFA");
        } else {
            input.next().find("input:first").css("background-color", "#FFFFFF");
            input.next().find("textarea:first").css("background-color", "#FFFFFF");
        }
    }

    function setAllInputReadOnly(readonly) {
        $("#maindata_id").find("input[cjoptions]").each(function (index, el) {
            setInputReadOnly($(this), readonly);
        });
    }

    function getInputReadOnlys() {
        els_readonly = [];
        $("#maindata_id").find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            if (co.readonly) {
                els_readonly.push($(this)[0]);
            }
        });
        // console.error(JSON.stringify(els_readonly));
    }

    //function onInputChange(field, oldvalue, newvalue) {
    //}

    this.reSetInputReadOnly = reSetInputReadOnly;
    function reSetInputReadOnly() {
        $("#main_tabs_id").find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            if ((co.readonly) || (edittype == TEditType.etSubmit) || (edittype == TEditType.etView)) {
                setInputReadOnly($(this), true);
            } else
                setInputReadOnly($(this), false);
        });
    }

    this.getFieldValue = function (fdname) {
        var v = $getInputValue("#maindata_id", fdname);
        if (v)
            return v;
        else
            return curMainData[fdname];
    }

    this.setFieldValue = function (fdname, value) {
        curMainData[fdname] = value;
        $setInputValue("#maindata_id", fdname, value);
    }
}


var JSONBandingFrm = new function () {

    this.clearMainData = function (sfilter, grids) {
        this.clearFrmData(sfilter);
        for (var i = 0; i < grids.length; i++) {
            $C.grid.clearData(grids[i]);
        }
    }

    this.clearFrmData = function (sfilter) {
        $(sfilter).find("input[cjoptions]").each(function (index, el) {
            $(this).textbox('setValue', "");
        });
    }
    this.fromJsonData = function (sfilter, jsondata) {
        $(sfilter).find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            var et = getInputType(el);
            var v = jsondata[co.fdname];
            switch (et) {
                case 1:
                {
                    if ((v == undefined) || (v == null) || (v == "NULL") || (v == "")) {
                        $(this).datetimebox('setValue', "");
                    } else {
                        var dt = (new Date()).fromStr(v);
                        $(this).datetimebox('setValue', dt.toUIDatestr());
                    }
                    break;
                }
                default:
                {
                    if ((v == undefined) || (v == null) || (v == "NULL"))
                        $(this).textbox('setValue', "")
                    else {
                        var iscom = false;
                        try {
                            var opt = $(this).textbox("options");
                            iscom = (("valueField" in opt) && ("textField" in opt) && ("data" in opt));
                        }
                        catch (e) {
                            //alert(e.message);
                        }
                        if ((co.dicgpid != undefined) || iscom) {
                            $(this).combobox('select', v);
                        } else
                            $(this).textbox('setValue', v);
                    }
                    break;
                }
            }
        });
        return true;
    }

    this.toJsonData = function (sfilter, jsondata, isnew) {
        $(sfilter).find("input[cjoptions]").each(function (index, el) {
            jsondata[$.c_parseOptions($(this)).fdname] = $(this).textbox("getValue");
        });
        //line datas
        if (!isnew)
            $(sfilter).c_setJsonDefaultValue(jsondata, false);
        return jsondata;
    }

    this.checkNotNull = function (sfilter, jsondata) {
        var msg = "";
        $(sfilter).find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            var et = getInputType(el);
            var v = jsondata[co.fdname];
            if ((co.required) && ((v == null) || (!v) || (v == ""))) {
                msg = msg + "<" + getInputLabel(sfilter, co.fdname).html() + "><br/>";
            }
        });
        if (msg == "") return true
        else {
            $.messager.alert('错误', msg + '不允许为空！', 'error');
            return false;
        }
    }
}

function $setInputValue(sfilter, fdname, value) {
    $(sfilter).find("input[cjoptions]").each(function (index, el) {
        var co = $.c_parseOptions($(this));
        if (co.fdname == fdname) {
            var et = getInputType(el);
            var v = value;
            switch (et) {
                case 1:
                {
                    if ((v == undefined) || (v == null) || (v == "NULL") || (v == "")) {
                        $(this).datetimebox('setValue', "");
                    } else {
                        var dt = (new Date()).fromStr(v);
                        $(this).datetimebox('setValue', dt.toUIDatestr());
                    }
                    break;
                }
                default:
                {
                    if ((v == undefined) || (v == null) || (v == "NULL"))
                        $(this).textbox('setValue', "")
                    else {
                        if (co.dicgpid) {
                            $(this).combobox('select', v);
                        } else
                            $(this).textbox('setValue', v);
                    }
                    break;
                }
            }
        }
    });
}

function $getInputValue(sfilter, fdname) {
    var v = undefined;
    $(sfilter).find("input[cjoptions]").each(function (index, el) {
        var co = $.c_parseOptions($(this));
        var et = getInputType(el);
        if (co.fdname == fdname) {
            v = $(this).textbox("getValue");
            return false;
        }
    });
    return v;
}

function getInputLabel(sfilter, fdname) {
    var lb = undefined;
    $(sfilter).find("td[cjoptions]").each(function (index, el) {
        var co = $.c_parseOptions($(this));
        if (co.fdname == fdname) {
            lb = $(this);
            return false;
        }
    });
    return lb;
}

function TSearchForm(sfrmOptions) {
    var inited = false;
    var id = sfrmOptions.id;
    if ((!id) || (id.length < 0)) {
        $.messager.alert('错误', '没有设置id变量!', 'error');
        return undefined;
    }
    var istree, htmlTempt, idField, treeField, singleSelect, enableIdpath, autoFind, singleAutoReturn, notFindAutoReturn, edittype, showTitle, orderStr;

    function readOptions() {
        istree = (sfrmOptions.isTree == undefined) ? false : sfrmOptions.isTree;
        htmlTempt = (sfrmOptions.htmlTempt) ? sfrmOptions.htmlTempt : "../templet/default/search_grid.html";
        idField = (sfrmOptions.idField) ? sfrmOptions.idField : undefined;
        treeField = (sfrmOptions.treeField) ? sfrmOptions.treeField : undefined;
        singleSelect = (sfrmOptions.singleSelect == undefined) ? true : sfrmOptions.singleSelect;
        enableIdpath = (sfrmOptions.enableIdpath == undefined) ? true : sfrmOptions.enableIdpath;
        autoFind = (sfrmOptions.autoFind == undefined) ? false : sfrmOptions.autoFind;
        singleAutoReturn = (sfrmOptions.singleAutoReturn == undefined) ? false : sfrmOptions.singleAutoReturn;
        notFindAutoReturn = (sfrmOptions.notFindAutoReturn == undefined) ? false : sfrmOptions.notFindAutoReturn;
        edittype = (sfrmOptions.edittype == undefined) ? TEditType.etFind : sfrmOptions.edittype;
        showTitle = (sfrmOptions.showTitle == undefined) ? true : sfrmOptions.showTitle;
        orderStr = (sfrmOptions.orderStr == undefined) ? "" : sfrmOptions.orderStr;
    }

    readOptions();
    var valuehtml = undefined;

    this.tag = 0;
    initPopFrm();
    this.TSearchForm = function (NsfrmOptions) {
        sfrmOptions = $.extend(sfrmOptions, NsfrmOptions);
        readOptions();
    }

    function initPopFrm() {
        $.ajax({
            url: htmlTempt,
            type: 'get',
            dataType: 'text',
            cache: false,
            async: true,
            contentType: "text/HTML; charset=utf-8",
            success: function (data) {
                //alert(data);
                if ($("#pop_search_divs").length == 0)
                    $("<div id='pop_search_divs'></div>").appendTo("body");
                data = data.replace("$pwid$", "'" + id + "'");
                var pw = $(data).appendTo("#pop_search_divs");
                valuehtml = getInputByField("value").parent().html();
                $("#" + id).find(".easyui-linkbutton[cjoptions]").each(function (index, el) {
                    var co = $.c_parseOptions($(this));
                    if (co.caction == "act_search") {
                        $(this).click(function () {
                            OnFindClick();
                        });
                    } else if (co.caction == "act_ok") {
                        $(this).click(function () {
                            OnOKClick();
                        });
                    } else if (co.caction == "act_select") {
                        $(this).click(function () {
                            OnSelectClick();
                        });
                    } else if (co.caction == "act_cancel") {
                        $(this).click(function () {
                            $("#" + id).window("close");
                        });
                    }
                });
                $.parser.parse(pw.parent());///////////////////////刷新整个界面？？
                var listgrid = $("#" + id).find("table[cjoptions]");
                var multiRow = (sfrmOptions.multiRow == undefined) ? false : sfrmOptions.multiRow;
                listgrid.datagrid({border: false,
                    singleSelect: !multiRow,
                    singleSelect: singleSelect,
                    onDblClickRow: onListDbClick,
                    columns: [sfrmOptions.gdListColumns]});
                if (istree) {

                }
                var item = getInputByField("item");
                if (!item) {
                    $.messager.alert('错误', '没有找到item录入框!', 'error');
                    return;
                }
                item.combobox({
                    data: sfrmOptions.gdListColumns,
                    valueField: 'field',
                    textField: 'title',
                    onSelect: onItemSelect
                });
                if (!showTitle) {
                    $("#" + id).find(".easyui-layout").each(function (index, el) {
                        if (index == 1) {
                            $(this).layout("remove", "north");
                        }
                    });
                }
                //show();
                inited = true;
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                $.messager.alert('错误', '获取Search模板文件错误!', 'error');
            }
        });
    }

    this.show = show;

    function show(clear) {
        if (!inited) {
            setTimeout(function () {
                show(clear);
            }, 100);
        } else doshow(clear);
    }

    function doshow(clear) {
        if (clear) {
            var listgrid = $("#" + id).find("table[cjoptions]");
            var multiRow = (sfrmOptions.multiRow == undefined) ? false : sfrmOptions.multiRow;
            $C.grid.clearData(listgrid);
            listgrid.datagrid({singleSelect: !multiRow});
        }
        if (autoFind) {
            OnFindClick();
        }
        $("#" + id).window("open");
    }

    function getInputByField(fdname) {
        var inpt = undefined;
        $("#" + id).find("input[cjoptions]").each(function (index, el) {
            var co = $.c_parseOptions($(this));
            var et = getInputType(el);
            if (co.fdname == fdname) {
                inpt = $(this);
                return;
            }
        });
        return inpt;
    }

    function onListDbClick() {
        OnOKClick();
    }


    function getFdItemdataByvalue(value) {
        if (!value) return undefined;
        var cdata = getInputByField("item").combobox("getData");
        for (var i = 0; i < cdata.length; i++) {
            var idata = cdata[i];
            if (idata["field"] == value)
                return idata;
        }
    }

    function onItemSelect(row) {
        var p = getInputByField("value").parent();
        p.html(valuehtml);
        $.parser.parse(p);
        var v = getInputByField("value");
        if (row.formatter) {
            var comurl = row.formatter("get_com_url");
            if (comurl) {
                v.combobox({
                    data: comurl.data,
                    valueField: comurl.valueField,
                    textField: comurl.textField
                });
            } else {
                //isdate?
            }
        } else {
            // v.textbox("setValue", "");
        }
    }

    function OnFindClick() {
        if (showTitle) {
            var fd = getInputByField("item").textbox("getValue");
            var v = getInputByField("value").textbox("getValue");
            var parms = (fd && v) ? [
                { parmname: fd, reloper: "like", parmvalue: v}
            ] : [];
        } else
            var parms = [];

        if (sfrmOptions.extParms)
            parms = parms.concat(sfrmOptions.extParms);
        var fdparms = {url: $C.cos.commonfind, type: "list", enableIdpath: enableIdpath, edittype: edittype, jpaclass: sfrmOptions.JPAClass, parms: parms};
        if (!$isEmpty(orderStr))
            fdparms.order = orderStr;

        var selfLine = (sfrmOptions.selfLine == undefined) ? true : sfrmOptions.selfLine;
        sfrmOptions.selfLine = selfLine;
        if (sfrmOptions.beforeFind) {
            if (!sfrmOptions.beforeFind(fdparms))
                return;
        }
        var url = fdparms.url + "?";
        for (var parm in fdparms) {
            if (typeof(fdparms[parm]) == "function")
                continue;
            if ((parm != "url") && (fdparms[parm] != undefined))
                if (parm == "parms")
                    url = url + "&parms=" + JSON.stringify(fdparms[parm]);
                else
                    url = url + "&" + parm + "=" + fdparms[parm];
        }
        // alert(url);
        /*       var url = fdparms.url + "?type=" + fdparms.type + "&selfline=" + selfLine + "&enableIdpath=" + enableIdpath + "&edittype=" + fdparms.edittype + "&jpaclass=" + fdparms.jpaclass;
         if (fdparms.order) {
         url = url + "&order=" + fdparms.order;
         }
         if (fdparms.parms.length > 0)
         url = url + "&parms=" + JSON.stringify(fdparms.parms);*/

        //console.error(url);
        $ajaxjsonget(url, function (jsdata) {
            if (((jsdata.length == 1) && singleAutoReturn) || ((jsdata.length == 0) && notFindAutoReturn)) {
                if (sfrmOptions.onResult)
                    sfrmOptions.onResult(jsdata);
                $("#" + id).window("close");
                return;
            }
            var listgrid = $("#" + id).find("table[cjoptions]");
            $C.grid.clearData(listgrid);
            listgrid.datagrid("loadData", jsdata);
        }, function () {
            //console.error("查询错误");
        });
    }

    function OnOKClick() {
        var listgrid = $("#" + id).find("table[cjoptions]");
        var rows = listgrid.datagrid('getSelections');
        if (rows.length == 0) {
            $.messager.alert('错误', '没有选择数据!', 'error');
        } else {
            if (sfrmOptions.onResult) {
                sfrmOptions.onResult(rows);
            }
            $("#" + id).window("close");
        }
    }

    function OnSelectClick() {
        var listgrid = $("#" + id).find("table[cjoptions]");
        var rows = listgrid.datagrid('getSelections');
        if (rows.length == 0) {
            listgrid.datagrid("selectAll");
        } else {
            listgrid.datagrid("unselectAll");
        }
    }

    //alert(2);
}

function getInputType(el) {
    var cls = $(el).attr("class");
    if (cls.indexOf("easyui-datetimebox") >= 0) {
        return 1;
    } else if (cls.indexOf("easyui-combobox") >= 0) {
        return 2;//combobox
    } else if (cls.indexOf("easyui-textbox") >= 0) {
        return 5;
    }
}

function onValueChange(newValue, oldValue) {
    if (mainline) {
        mainline.setDataChanged(true);
    }
}