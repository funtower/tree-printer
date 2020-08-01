// 重新渲染，参数为空则代表从根节点开始往下渲染整棵树，参数不为空，则渲染从这个节点开始以下的树
function reRenderTree(node = root) {
	JTopo.layout.layoutNode(scene, node, true);
	scene.addEventListener('mouseup', function (e) {
		if (e.target && e.target.layout) {
			JTopo.layout.layoutNode(scene, e.target, true);
		}
	});
}

// 构建树元素（节点、连线）
function buildTree(tree) {
	let nodes = tree.nodes;
	let lines = tree.lines;
	for (let el of nodes) {
		let node = insertNode(el);
		if (el.data == tree.root.data) {
			root = node;
		}
		allNodes.set(el.data, node);
	}
	for (let el of lines) {
		let fromNode = el.from;
		let toNode = el.to;
		let from = allNodes.get(fromNode.data);
		let to = allNodes.get(toNode.data);
		console.log('from: ' + from + 'to：' + to);
		connectLine(from, to);
	}
}

// 插入节点
function insertNode(el) {
	let node = new JTopo.CircleNode(el.data);
	node.radius = 15;
	debugger;
	node.fillColor = '68,255,255';
	node.alpha = 0.75;
	let horizontal = canvasWidth * el.horizontalOffsetPercent;
	let vertical = canvasHeight * el.verticalOffsetPercent;
	node.setLocation(horizontal, vertical);
	node.layout = { type: 'tree', width: canvasWidth / (1 << el.depth), height: 100 }
	scene.add(node);
	allNodes.set(node.text, node);
	return node;
}

// 将节点之间建立连线
function connectLine(from, to) {
	let link = new JTopo.Link(from, to);
	link.arrowsRadius = 8;
	link.alpha = 0.75;
	scene.add(link);
}

function renderTree() {
	scene.clear();
	inputData = new Array();
	axios.get(baseUrl + "/print/63")
		.then(function (response) {
			console.log("根节点：" + response.data.root.data);
			let tree = response.data;
			buildTree(tree);
			reRenderTree();
		})
		.catch(function (error) {
			console.log(error);
		});
}

// 页面工具栏
function showJTopoToobar(stage) {
	var toobarDiv = $('<div class="jtopo_toolbar"/>').html(''
		+ '&nbsp;&nbsp;<input type="button" id="centerButton" value="居中显示"/>'
		+ '&nbsp;&nbsp;<input type="button" id="ascBatchInsert63" value="顺序插入63个数"/>'
		+ '&nbsp;&nbsp;<input type="text" id="findText" style="width: 100px;" value="" onkeydown="enterPressHandler(event)">'
		+ '<input type="button" id="findButton" value=" 查 询 ">'
		+ '&nbsp;&nbsp;<input type="text" id="insertText" style="width: 100px;"">'
		+ '<input type="button" id="insertButton" value=" 插 入 ">'
		+ '&nbsp;&nbsp;<input type="text" id="deleteText" style="width: 100px;" value=""">'
		+ '<input type="button" id="deleteButton" value=" 删 除 ">'
		+ '&nbsp;&nbsp;<input type="button" id="exportButton" value="导出PNG">'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;<span>防止节点位置错乱，空节点用[NIL]表示</span>'
	);

	$('#content').prepend(toobarDiv);

	// 工具栏按钮处理
	$('#centerButton').click(function () {
		stage.centerAndZoom(); //缩放并居中显示
	});
	// 演示顺序插入63个数
	$('#ascBatchInsert63').click(function () {
		renderTree();
	});
	$('#exportButton').click(function () {
		stage.saveImageInfo();
	});

	window.enterPressHandler = function (event) {
		if (event.keyCode == 13 || event.which == 13) {
			$('#findButton').click();
		}
	};
	// 查询
	$('#findButton').click(function () {
		var text = $('#findText').val().trim();
		//var nodes = stage.find('node[text="'+text+'"]');
		var scene = stage.childs[0];
		var nodes = scene.childs.filter(function (e) {
			return e instanceof JTopo.Node;
		});
		nodes = nodes.filter(function (e) {
			if (e.text == null) return false;
			return e.text.indexOf(text) != -1;
		});

		if (nodes.length > 0) {
			var node = nodes[0];
			node.selected = true;
			var location = node.getCenterLocation();
			// 查询到的节点居中显示
			stage.setCenter(location.x, location.y);

			function nodeFlash(node, n) {
				if (n == 0) {
					node.selected = false;
					return;
				};
				node.selected = !node.selected;
				setTimeout(function () {
					nodeFlash(node, n - 1);
				}, 300);
			}

			// 闪烁几下
			nodeFlash(node, 6);
		}
	});
	// 删除
	$('#deleteButton').click(function () {
		let deleteText = $('#deleteText').val().trim();
		let scene = stage.childs[0];
		let nodes = scene.childs.filter(function (e) {
			return e instanceof JTopo.Node;
		});
		nodes = nodes.filter(function (e) {
			if (e.text == null) return false;
			return e.text.indexOf(deleteText) != -1;
		});

		if (nodes.length == 0) {
			$('#deleteText').val('');
			return;
		}
		if (inputData.length == 0) {
			alert('只能删除你自己新增的节点，演示节点不让删！现在清空画布，你先新增节点然后再删除');
			scene.clear();
		}

		let node = nodes[0];
		let del = deleteText;

		axios.post(baseUrl + "/print/delete", { inputData, del })
			.then(function (response) {
				let tree = response.data;
				scene.clear();
				inputData = new Array();
				if (!tree) {
					$('#deleteText').val('');
					root = null;
					return;
				}
				for (const nodeVo of tree.nodes) {
					let content = nodeVo.data;
					if (content.indexOf("NIL") != -1) {
						continue;
					}
					let val = content.substring(1, content.length - 1);
					inputData.push(val);
				}
				buildTree(tree);
				reRenderTree();
			})
			.catch(function (error) {
				console.log(error);
			});
		$('#deleteText').val('');

	});
	// 插入
	$('#insertButton').click(function () {
		let insert = $('#insertText').val().trim();
		if (!(insert && insert % 1 === 0 && inputData.indexOf(insert) == -1)) {
			$('#insertText').val('');
			return;
		}
		if (inputData.length == 0) {
			scene.clear();
		}
		inputData.push(insert);

		axios.post(baseUrl + "/print/insert", { inputData, insert })
			.then(function (response) {
				scene.clear();
				let tree = response.data;
				buildTree(tree);
				reRenderTree();
			})
			.catch(function (error) {
				console.log(error);
			});
		$('#insertText').val('');
	});
}